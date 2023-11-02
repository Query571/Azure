package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.*;
import com.azureAccelerator.entity.AzureInstanceTypes;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.repository.AzureInstanceTypesRepository;
import com.azureAccelerator.service.AKSDeployService;
import com.azureAccelerator.service.IntegratingAKStoACRService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.azureAccelerator.util.GetToken;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.containerservice.*;
import com.microsoft.azure.management.resources.ResourceGroup;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Service
public class AKSDeployServiceImpl implements AKSDeployService {

  //private static final Logger logger = Logger.getLogger(AKSDeployServiceImpl.class);
  private static final Logger logger = LogManager.getLogger(AKSDeployServiceImpl.class);
  private final VaultService vaultService;
  private final ApplicationProperties applicationProperties;
  private final AzureInstanceTypesRepository azureInstanceTypesRepository;
  private final UserService userService;
  private final IntegratingAKStoACRService integratingAKStoACRService;

  @Autowired
  public AKSDeployServiceImpl(VaultService vaultService,
                              ApplicationProperties applicationProperties,
                              AzureInstanceTypesRepository azureInstanceTypesRepository, UserService userService, IntegratingAKStoACRService integratingAKStoACRService) {
    this.vaultService = vaultService;
    this.applicationProperties = applicationProperties;
    this.azureInstanceTypesRepository = azureInstanceTypesRepository;
    this.userService = userService;
    this.integratingAKStoACRService = integratingAKStoACRService;
  }

  @Override
  public AKSDeployResponse deployAKS(HttpServletRequest request, AKSDeployDto aksDeployDto) throws Exception {

    KubernetesCluster kubernetesCluster;

    List<AKSNodePool> aksNodePoolList =  new ArrayList<>();
    logger.info("AKS creation started...");
    try {

      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure()
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());
      /*List<KubernetesCluster> list=azure.kubernetesClusters().listByResourceGroup(aksDeployDto.getResourceGroup());
      for(KubernetesCluster cluster:list) {
        if (cluster.name().equals(aksDeployDto.getAksName())) {
          throw new Exception("KubernetesCluster name is already present. Try With Unique name");
        }
      }*/

      if(aksDeployDto.getAgentPoolMode().equalsIgnoreCase("System")){
        kubernetesCluster = createAKSCluster(request,aksDeployDto, credentials, azure);
        logger.debug(kubernetesCluster);
      } else {
         kubernetesCluster = createAKSCluster(request,aksDeployDto, credentials, azure);
        logger.debug(kubernetesCluster);
         // add user node pool
        String status = createUserPoolMode(request,aksDeployDto,kubernetesCluster);
        logger.debug("userMode script status ::"+status);
       }


      KubernetesCluster kubernetesClusterRes = azure.kubernetesClusters().getById(kubernetesCluster.id());

      logger.info(
          "kubernetesCluster.provisioningState()  ::" + kubernetesClusterRes.provisioningState());
      kubernetesClusterRes.agentPools().forEach((s, kubernetesClusterAgentPool) -> {
        AKSNodePool aksNodePool = new AKSNodePool();
        aksNodePool.setName(kubernetesClusterAgentPool.name());
        aksNodePool.setVmSize(kubernetesClusterAgentPool.vmSize().toString());
        aksNodePool.setVmCount(kubernetesClusterAgentPool.count());
        aksNodePool.setPoolMode(kubernetesClusterAgentPool.mode().toString());
        aksNodePool.setOsType(kubernetesClusterAgentPool.osType().toString());
        aksNodePool.setAgentPoolType(kubernetesClusterAgentPool.type().toString());
        aksNodePoolList.add(aksNodePool);
      });
      if (aksDeployDto.getAcrName()!=null) {
        integratingAKStoACR(request,aksDeployDto.getResourceGroup(), aksDeployDto.getAksName(), aksDeployDto.getAcrName());
      }
      //Deply AKS Dashboard..
      aksDashboardDeploy(request,aksDeployDto);

      logger.debug(aksNodePoolList);
      logger.info("AKS creation is ended");
    }catch (CloudException e){
      logger.error("deployAKS Exception : "+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return
        AKSDeployResponse
            .builder()
            .aksId(kubernetesCluster.id())
            .aksName(kubernetesCluster.name())
            .location(kubernetesCluster.regionName())
            .status(kubernetesCluster.provisioningState())
            .resourceGroup(kubernetesCluster.resourceGroupName())
            .kubernetesVersion(kubernetesCluster.version().toString())
            /*.networkType(kubernetesCluster.networkProfile().networkPlugin().toString())
            .dnsServiceIP(kubernetesCluster.networkProfile().dnsServiceIP())
            .podCidr(kubernetesCluster.networkProfile().podCidr())
            .serviceCidr(kubernetesCluster.networkProfile().serviceCidr())
            .dockerBridgeCidr(kubernetesCluster.networkProfile().dockerBridgeCidr())*/
            .aksNodePool(aksNodePoolList)
            .build();

  }

  private void aksDashboardDeploy(HttpServletRequest request,AKSDeployDto aksDeployDto) throws JSONException {
    List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

    var secret =
            vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
    logger.info("AKS Dashboard Deploy is started...");

    try {

      ProcessBuilder processBuilder = new
              ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "aksDashboardDeploy.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),aksDeployDto.getResourceGroup(),aksDeployDto.getAksName());
      logger.info("cmdList "+processBuilder.command());

      Process process = new
              ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "aksDashboardDeploy.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),aksDeployDto.getResourceGroup(),aksDeployDto.getAksName()).start();
      logger.debug("After Process");


      BufferedReader Reader = new BufferedReader(new InputStreamReader(
              process.getErrorStream()));
      logger.debug(Reader.readLine());
      if (process.waitFor() != 0) {
        logger.error("aksDashboardDeploy() ::" + Reader.readLine());
        throw new AzureAcltrRuntimeException(
                Reader.readLine(),
                null,
                Reader.readLine(),
                HttpStatus.INTERNAL_SERVER_ERROR);
      } else {
        BufferedReader response = new BufferedReader(new InputStreamReader(
                process.getInputStream()));
        logger.debug(response.readLine());
        logger.info("aksDashboardDeploy Script Exc Ended...");

      }
    } catch (Exception e) {
      logger.error("aksDashboardDeploy Exception : "+e.getMessage());
      throw new AzureAcltrRuntimeException(
              e.getMessage(),
              null,
              e.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private KubernetesCluster createAKSCluster(HttpServletRequest request,AKSDeployDto aksDeployDto,
      AzureCredentials credentials, Azure azure) throws Exception {


    KubernetesCluster kubernetesCluster;
    try{
    kubernetesCluster = azure.kubernetesClusters()
        .define(aksDeployDto.getAksName())
        .withRegion(aksDeployDto.getLocation())
        .withExistingResourceGroup(aksDeployDto.getResourceGroup())
        .withLatestVersion()
        .withRootUsername("azure-user")
        .withSshKey("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDCnOlkf17Azlm0vUVAdFXBrWiiWh7+AZ7QPDJfPvs1oRTgzJMW+XUbzjiqgsWoyhOLq2OCu2VfDrVzwxqpxGUpWJXxmWFhOXqLiEB3SyOGUfAQWECbde2NF1BBuqzyJ6mCIUm0KifWDacoUV1G6dAbhyRx28fSk+ZqArBNidakU4sKQl0u1S0ID6RzSOPX/QWl+XoFmIusVHciwW9Z1xGpZVMNVIw7LKX1Hxs8y2eR9XqqTw4GC3o2B5fR09eM5KXukpEbsfUjroi+47mAEim/jVfj6xnMHEpgnhE5pBdzr47C+kjUV+KyEI1iDmLx88TdwrcxcNvm9/TFdxz90TLb ubuntu@ubuntu-cse")
        .withServicePrincipalClientId(credentials.getApplicationTokenCredentials().clientId())
        .withServicePrincipalSecret(credentials.getApplicationTokenCredentials().clientSecret())
        .defineAgentPool("agentpool")
        .withVirtualMachineSize(ContainerServiceVMSizeTypes.fromString(aksDeployDto.getVmSize()))
        .withAgentPoolTypeName("AZX-nodePool")
        .withAgentPoolVirtualMachineCount(aksDeployDto.getVmCount())
        .withMode(AgentPoolMode.SYSTEM)
        .withMaxPodsCount(aksDeployDto.getMaxPodsCount())
        .withOSDiskSizeInGB(aksDeployDto.getDiskSizeInGB())
        .withOSType(OSType.LINUX)
        .withAgentPoolType(AgentPoolType.VIRTUAL_MACHINE_SCALE_SETS)
        .attach()
        .defineNetworkProfile()
        .withNetworkPlugin(NetworkPlugin.AZURE)
        .withNetworkPolicy(NetworkPolicy.AZURE)
        .attach()
        .withAddOnProfiles(aksDeployDto.getAddOnProfileMap())
        .withTag("Application", "AZX")
        .withTag("Name", aksDeployDto.getAksName())
        .withDnsPrefix("dns-" + "AZX")
        .create();
    logger.debug(kubernetesCluster);
  } catch (CloudException e) {
      logger.error("createAKSCluster Exception : "+e.getMessage());
    throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
            HttpStatus.INTERNAL_SERVER_ERROR);
  }
    return kubernetesCluster;
  }

  private String createUserPoolMode(HttpServletRequest request,AKSDeployDto aksDeployDto, KubernetesCluster kubernetesCluster) throws JSONException {

    List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

    var secret =
            vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

    try {

      logger.info("User NodePool creation is Started...");

      ProcessBuilder processBuilder = new
              ProcessBuilder("bash",
              applicationProperties.getAppScriptPath() + "userNodePool.sh",
              secret.getClientId(),
              secret.getClientSecret(),
              secret.getTenantId(),
              kubernetesCluster.name(),
              "usernp",
              aksDeployDto.getResourceGroup(),
              String.valueOf(aksDeployDto.getVmCount()),
              aksDeployDto.getSystemType(),
              ContainerServiceVMSizeTypes.fromString(aksDeployDto.getVmSize()).toString(),
              String.valueOf(aksDeployDto.getMaxPodsCount()),
              String.valueOf(aksDeployDto.getDiskSizeInGB()));
      logger.debug("cmdList "+processBuilder.command());

      Process process = new
              ProcessBuilder("bash",
              applicationProperties.getAppScriptPath() + "userNodePool.sh",
              secret.getClientId(),
              secret.getClientSecret(),
              secret.getTenantId(),
              kubernetesCluster.name(),
              "usernp",
              aksDeployDto.getResourceGroup(),
              String.valueOf(aksDeployDto.getVmCount()),
              aksDeployDto.getSystemType(),
              ContainerServiceVMSizeTypes.fromString(aksDeployDto.getVmSize()).toString(),
              String.valueOf(aksDeployDto.getMaxPodsCount()),
              String.valueOf(aksDeployDto.getDiskSizeInGB())).start();
      logger.debug("After Process");

      BufferedReader Reader = new BufferedReader(new InputStreamReader(
          process.getErrorStream()));
      if (process.waitFor() != 0) {
        logger.error("createUserPoolMode() ::"+Reader.readLine());
        throw new AzureAcltrRuntimeException(
            Reader.readLine(),
            null,
            Reader.readLine(),
            HttpStatus.INTERNAL_SERVER_ERROR);
      } else {
        BufferedReader response = new BufferedReader(new InputStreamReader(
            process.getInputStream()));
        logger.info("createUserPoolMode Script Exc Ended...");
        return response.readLine();
      }
    } catch (Exception e) {
      logger.error("createUserPoolMode Exception : "+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.getMessage(),
          null,
          e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public List<AKSDeployResponse> getAKSClusters(HttpServletRequest request,String resourceGroupName) throws JSONException {

    List<AKSDeployResponse> aksDeployResponseList = new ArrayList<>();
    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure()
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());

      logger.info("Listing AKS Clusters are started");
      Base64.Encoder encoder = Base64.getEncoder();

      PagedList<KubernetesCluster> kubernetesClusterList = azure.kubernetesClusters()
          .listByResourceGroup(resourceGroupName);
      logger.debug(kubernetesClusterList);
      for (KubernetesCluster kubernetesCluster :kubernetesClusterList) {
        List<AKSNodePool> aksNodePoolList =  new ArrayList<>();
        kubernetesCluster.agentPools().forEach((s, kubernetesClusterAgentPool) -> {
          AKSNodePool aksNodePool = new AKSNodePool();
          aksNodePool.setName(kubernetesClusterAgentPool.name());
          aksNodePool.setVmSize(kubernetesClusterAgentPool.vmSize().toString());
          aksNodePool.setVmCount(kubernetesClusterAgentPool.count());
          aksNodePool.setPoolMode(kubernetesClusterAgentPool.mode().toString());
          aksNodePool.setOsType(kubernetesClusterAgentPool.osType().toString());
          aksNodePool.setAgentPoolType(kubernetesClusterAgentPool.type().toString());
          aksNodePool.setProvisioningState(kubernetesClusterAgentPool.inner().provisioningState());
          aksNodePool.setPowerState(kubernetesClusterAgentPool.inner().powerState().code().toString());
          aksNodePool.setVersion(kubernetesClusterAgentPool.inner().orchestratorVersion());
          aksNodePoolList.add(aksNodePool);
        });
        AKSDeployResponse aksDeployResponse = AKSDeployResponse
            .builder()
            .aksId(kubernetesCluster.id())
            .aksName(kubernetesCluster.name())
            .location(kubernetesCluster.regionName())
            .status(kubernetesCluster.provisioningState())
            .powerState(kubernetesCluster.inner().powerState().code().toString())
            .resourceGroup(kubernetesCluster.resourceGroupName())
            .kubernetesVersion(encoder.encodeToString(String.valueOf(kubernetesCluster.version().toString()).getBytes()))
            .isEnabled(kubernetesCluster.addonProfiles())
            /*.networkType(kubernetesCluster.networkProfile().networkPlugin().toString())
            .dnsServiceIP(kubernetesCluster.networkProfile().dnsServiceIP())
            .podCidr(kubernetesCluster.networkProfile().podCidr())
            .serviceCidr(kubernetesCluster.networkProfile().serviceCidr())
            .dockerBridgeCidr(kubernetesCluster.networkProfile().dockerBridgeCidr())*/
            .aksNodePool(aksNodePoolList)
            .build();
            logger.debug("Aks CLuster : "+aksDeployResponse);
        aksDeployResponseList.add(aksDeployResponse);
      }
      logger.debug(aksDeployResponseList);
      logger.info("AKS cluster is Ended...");
    } catch (CloudException e) {
      logger.error("error occurred while fetching getAKSClusters ::::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return aksDeployResponseList;
  }

    @Override
  public List<AzureInstanceTypes> getAzureVMSizes(HttpServletRequest request) {
    return
        azureInstanceTypesRepository.findAllByVCoreGreaterThanEqualAndMemoryInGBGreaterThanEqual(2,4);
  }

  @Override
  public void deleteAKS(HttpServletRequest request,String aksId) throws Exception {
    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure()
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());
      logger.info("Aks is deleting...");
      KubernetesCluster kubernetesCluster=azure.kubernetesClusters().getById(aksId);
      logger.debug(kubernetesCluster.name()+ " is deleting");
      if (kubernetesCluster!=null) {
        azure.kubernetesClusters().deleteById(aksId);
      }else{
        throw new Exception(aksId+" details are not found");
      }
      logger.info("Aks deleted");

    } catch (CloudException e) {
      logger.error("delete Aks Exception : "+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }

  @Override
  public String getRGLocation(HttpServletRequest request,String resourceGroupName) throws JSONException {
    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure()
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());

      ResourceGroup resourceGroup =
          azure
              .resourceGroups()
              .getByName(resourceGroupName);

      return resourceGroup.regionName();

    } catch (CloudException e) {
      logger.error("error occurred  while getting getRGLocation:::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public AzureCommonResponseDto deployAppOnAKS(HttpServletRequest request,
      String resourceGroupName,
      String aksName,
      MultipartFile file) throws JSONException {
    List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));
    var secret =
            vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

    try {

      if (file.isEmpty()) {
       throw new AzureAcltrRuntimeException(
            "File is empty, Please check the file content and try again.",
            null,
            "File is empty, Please check the file content and try again.",
            HttpStatus.NOT_FOUND);
      }

      byte[] bytes = file.getBytes();
      Path path = Paths.get(applicationProperties.getAppDeployPath() + file.getOriginalFilename());
      logger.debug(file.getOriginalFilename());
      Files.write(path, bytes);

      Set<PosixFilePermission> per = new HashSet<PosixFilePermission>();
      per.add(PosixFilePermission.GROUP_READ);
      per.add(PosixFilePermission.GROUP_EXECUTE);
      per.add(PosixFilePermission.OWNER_READ);
      per.add(PosixFilePermission.OWNER_EXECUTE);

      Files.setPosixFilePermissions(path,per);
      logger.debug(file.getOriginalFilename()+" upload to "+path);

      logger.info("aksAppDeploy Script Exc Started...");
      List<String> cmdList = new ArrayList<>();

          cmdList.add("bash");
          cmdList.add(applicationProperties.getAppScriptPath() + "aksAppDeploy.sh");
          cmdList.add(secret.getClientId());
          cmdList.add(secret.getClientSecret());
          cmdList.add(secret.getTenantId());
          cmdList.add(resourceGroupName);
          cmdList.add(aksName);
          cmdList.add(file.getOriginalFilename());

          ProcessBuilder processBuilder = new
              ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "aksAppDeploy.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),resourceGroupName,aksName,file.getOriginalFilename());
          logger.info("cmdList "+processBuilder.command());

          Process process = new
              ProcessBuilder("bash",
              applicationProperties.getAppScriptPath() + "aksAppDeploy.sh",
              secret.getClientId(),
              secret.getClientSecret(),
              secret.getTenantId(),
              resourceGroupName,
              aksName,
              file.getOriginalFilename()).start();
      logger.debug("After Process");


      BufferedReader Reader = new BufferedReader(new InputStreamReader(
          process.getErrorStream()));
      logger.info("process.waitFor() ::"+process.waitFor());
      if (process.waitFor() != 0) {
       logger.error("deployAppOnAKS() ::"+Reader.readLine());
        throw new AzureAcltrRuntimeException(
            Reader.readLine(),
            null,
            Reader.readLine(),
            HttpStatus.INTERNAL_SERVER_ERROR);
      } else {
        BufferedReader response = new BufferedReader(new InputStreamReader(
            process.getInputStream()));

        logger.info("aksAppDeploy Script Exc Ended...");

        return AzureCommonResponseDto.builder().name(response.readLine()).build();
      }

    } catch (Exception e) {
      logger.error("aksAppDeploy Exception : "+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.getMessage(),
          null,
          e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public AzureCommonResponseDto addNodePool(HttpServletRequest request,AddNodePoolDto addNodePoolDto) throws JSONException {
    List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

    var secret =
            vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());


    //Process process;
    try {
      logger.info("add node pool Script Exc Started..."+addNodePoolDto.getAksName());
      List<String> cmdList = new ArrayList<>();
      cmdList.add("bash");
      cmdList.add(applicationProperties.getAppScriptPath()+"userNodePool.sh");
      cmdList.add(secret.getClientId());
      cmdList.add(secret.getClientSecret());
      cmdList.add(secret.getTenantId());
      cmdList.add(addNodePoolDto.getAksName());
      cmdList.add(addNodePoolDto.getNodePoolName());
      cmdList.add(addNodePoolDto.getResourceGroupName());
      cmdList.add(String.valueOf(addNodePoolDto.getNodeCount()));
      cmdList.add(addNodePoolDto.getSystemType());
      cmdList.add(ContainerServiceVMSizeTypes.fromString(addNodePoolDto.getNodeSize()).toString());
      cmdList.add(String.valueOf(addNodePoolDto.getMaxPods()));
      cmdList.add(String.valueOf(addNodePoolDto.getNodeDiskSize()));

      //process = new ProcessBuilder(cmdList).start();
      String cmd = "bash "+applicationProperties.getAppScriptPath() + "userNodePool.sh";

      ProcessBuilder processBuilder = new
              ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "userNodePool.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),
              addNodePoolDto.getAksName(),
              addNodePoolDto.getNodePoolName(),
              addNodePoolDto.getResourceGroupName(),
              String.valueOf(addNodePoolDto.getNodeCount()),
              addNodePoolDto.getSystemType(),
              ContainerServiceVMSizeTypes.fromString(addNodePoolDto.getNodeSize()).toString(),
              String.valueOf(addNodePoolDto.getMaxPods()),
              String.valueOf(addNodePoolDto.getNodeDiskSize()));
      logger.info("cmdList "+processBuilder.command());

      Process process = new
              ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "userNodePool.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),
              addNodePoolDto.getAksName(),
              addNodePoolDto.getNodePoolName(),
              addNodePoolDto.getResourceGroupName(),
              String.valueOf(addNodePoolDto.getNodeCount()),
              addNodePoolDto.getSystemType(),
              ContainerServiceVMSizeTypes.fromString(addNodePoolDto.getNodeSize()).toString(),
              String.valueOf(addNodePoolDto.getMaxPods()),
              String.valueOf(addNodePoolDto.getNodeDiskSize())).start();
      logger.debug("After Process");
      BufferedReader Reader = new BufferedReader(new InputStreamReader(
              process.getErrorStream()));
      logger.info("addNodePool Status Code :"+process.waitFor());
      if (process.waitFor() != 0) {
        StringBuilder stringRes = new StringBuilder();
        String line="";
        while ((line=Reader.readLine())!=null) {
          stringRes.append(line);
        }
        logger.error("addNodePool  Error Line ::"+stringRes);
        throw new AzureAcltrRuntimeException(
                stringRes.toString(),
                null,
                stringRes.toString(),
                HttpStatus.INTERNAL_SERVER_ERROR);
      } else {
        BufferedReader response = new BufferedReader(new InputStreamReader(
                process.getInputStream()));
       logger.info("add node pool Script Exc Ended...");
        return AzureCommonResponseDto.builder().name(response.readLine()).build();
      }
    } catch (Exception e) {
      logger.error("Error occured while add node pool : "+e.getMessage());
      throw new AzureAcltrRuntimeException(
              e.getMessage(),
              null,
              e.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public AzureCommonResponseDto scaleNode(HttpServletRequest request,ScaleAKSNodePool scaleAKSNodePool) throws JSONException {
    List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

    var secret =
            vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

    try {
      logger.info("scaleNode Script Exc Started...");
      ProcessBuilder processBuilder = new
              ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "nodescale.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),
              scaleAKSNodePool.getResourceGroupName(),
              scaleAKSNodePool.getAksName(),
              scaleAKSNodePool.getNodePoolName(),
              String.valueOf(scaleAKSNodePool.getNodeCount()));
      logger.debug("cmdList "+processBuilder.command());

      Process process = new
              ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "nodescale.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),
              scaleAKSNodePool.getResourceGroupName(),
              scaleAKSNodePool.getAksName(),
              scaleAKSNodePool.getNodePoolName(),
              String.valueOf(scaleAKSNodePool.getNodeCount())).start();
      logger.debug("After Process");
      BufferedReader Reader = new BufferedReader(new InputStreamReader(
              process.getErrorStream()));
      logger.info("Status Code :"+process.waitFor());
      if (process.waitFor() != 0) {
        StringBuilder stringRes = new StringBuilder();
        String line="";
        while ((line=Reader.readLine())!=null) {
          stringRes.append(line);
        }
        logger.error("Error Line ::"+stringRes);
        throw new AzureAcltrRuntimeException(
            stringRes.toString(),
            null,
            stringRes.toString(),
            HttpStatus.INTERNAL_SERVER_ERROR);
      } else {
        BufferedReader response = new BufferedReader(new InputStreamReader(
                process.getInputStream()));
        logger.info("scaleNode Script Exc Ended...");
        return AzureCommonResponseDto.builder().name(response.readLine()).build();
      }
    } catch (Exception e) {
      logger.error("Error occurred while scaleNode : "+e.getMessage());
      throw new AzureAcltrRuntimeException(
              e.getMessage(),
              null,
              e.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }

  @Override
  public AzureCommonResponseDto upgradeKubernetVersion(HttpServletRequest request,UpgrKuberNetVerDto upgrKuberNetVerDto) throws JSONException {
    List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

    var secret =
            vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());


    //Process process;
    try {
      logger.info("aksUpgrade Script Exc Started...");
      ProcessBuilder processBuilder = new
              ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "aksupgrade.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),
              "upgrade",
              upgrKuberNetVerDto.getResourceGroupName(),
              upgrKuberNetVerDto.getAksName(),
              upgrKuberNetVerDto.getVersion());
      logger.debug("cmdList "+processBuilder.command());

      Process process = new
              ProcessBuilder("bash",
              applicationProperties.getAppScriptPath() + "aksupgrade.sh",
              secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),
              "upgrade",
              upgrKuberNetVerDto.getResourceGroupName(),
              upgrKuberNetVerDto.getAksName(),
              upgrKuberNetVerDto.getVersion()).start();
      logger.debug(process);
      logger.debug("After Process");

      BufferedReader Reader = new BufferedReader(new InputStreamReader(
              process.getErrorStream()));
      logger.info("Status Code :"+process.waitFor());
      if (process.waitFor() != 0) {
        StringBuilder stringRes = new StringBuilder();
        String line="";
        while ((line=Reader.readLine())!=null) {
          stringRes.append(line);
        }
        logger.error("Error Line ::"+stringRes.toString());
        throw new AzureAcltrRuntimeException(
            stringRes.toString(),
            null,
            stringRes.toString(),
            HttpStatus.INTERNAL_SERVER_ERROR);
      } else {
        BufferedReader response = new BufferedReader(new InputStreamReader(
                process.getInputStream()));
        logger.info("aksUpgrade Script Exc Ended...");
       return AzureCommonResponseDto.builder().name(response.readLine()).build();
      }
    } catch (Exception e) {
      logger.error("Error occurred while aksUpgrade Exception : "+e.getMessage());
      throw new AzureAcltrRuntimeException(
              e.getMessage(),
              null,
              e.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public AKSDashboardDto getKbrntDashboard(HttpServletRequest request,String resourceGroupName, String aksName) throws JSONException {
    List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

    var secret =
            vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());


    List<String> stringList= new ArrayList<>();

    try {
      integratingAKStoACRService.dashboardDeploy(request,resourceGroupName,aksName);
      logger.info("aksDashboardIP Script Exc Started...");

      ProcessBuilder processBuilder = new
              ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "aksDashboardIP.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),resourceGroupName,aksName);
      logger.debug("cmdList "+processBuilder.command());

      Process process = new
              ProcessBuilder("bash",
              applicationProperties.getAppScriptPath() + "aksDashboardIP.sh",
              secret.getClientId(),
              secret.getClientSecret(),
              secret.getTenantId(),
              resourceGroupName,aksName).start();
      logger.debug("After Process");


      BufferedReader Reader = new BufferedReader(new InputStreamReader(
              process.getErrorStream()));
      if (process.waitFor() != 0) {
        logger.error("Error Reader.readLine() ::"+Reader.readLine());
        throw new AzureAcltrRuntimeException(
                Reader.readLine(),
                null,
                Reader.readLine(),
                HttpStatus.INTERNAL_SERVER_ERROR);
      } else {
        BufferedReader response = new BufferedReader(new InputStreamReader(
                process.getInputStream()));
        logger.info("aksDashboardIP Script Exc Ended...");
        String line="";
        while ((line=response.readLine())!=null) {
          stringList.add(line);
        }
       return AKSDashboardDto.builder()
               .dashboardIp(stringList.get(0))
               .token(stringList.get(1))
               .build();

      }
    } catch (Exception e) {
      logger.error("Error occurred while aksDashboardIP Exception : "+e.getMessage());

      throw new AzureAcltrRuntimeException(
              e.getMessage(),
              null,
              e.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public Map<String, String> aksStart(HttpServletRequest request, String resourceGroupName, String aksName) throws JSONException, IOException {

    String output=null;
    String inputLine=null;
    StringBuffer response = new StringBuffer();
    Map<String,String> map=new HashMap<>();

    AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));

    URL url=new URL("https://management.azure.com/subscriptions/"+credentials.getSubscriptionId()+
            "/resourceGroups/"+resourceGroupName+
            "/providers/Microsoft.ContainerService/managedClusters/"+aksName+"/start?api-version=2022-04-01");

    GetToken getToken = new GetToken(vaultService,applicationProperties, userService);
    logger.debug("url : " + url.toString());


    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "application/json");

    conn.setRequestProperty("Authorization", "bearer " + getToken.gettingToken(request.getHeader("userName")));
    String properties="";
    byte[] properties1 = properties.getBytes(StandardCharsets.UTF_8);

    OutputStream stream = conn.getOutputStream();

    stream.write(properties1);

    logger.debug(conn.getResponseCode() + "-----" + conn.getResponseMessage());


    if (conn.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED || conn.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT ) { //success

      BufferedReader in1 = new BufferedReader(new InputStreamReader(
              conn.getInputStream()));

      while ((inputLine = in1.readLine()) != null) {
        response.append(inputLine);
      }
      logger.debug(response);

      map.put("Status", aksName+" is started successfully");

      in1.close();
    }
    conn.disconnect();

    return map;
  }
  @Override
  public Map<String, String> aksStop(HttpServletRequest request, String resourceGroupName, String aksName) throws JSONException, IOException {

    String output=null;
    String inputLine=null;
    StringBuffer response = new StringBuffer();
    Map<String,String> map=new HashMap<>();

    AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));

    URL url=new URL("https://management.azure.com/subscriptions/"+credentials.getSubscriptionId()+
            "/resourceGroups/"+resourceGroupName+
            "/providers/Microsoft.ContainerService/managedClusters/"+aksName+"/stop?api-version=2022-04-01");

    GetToken getToken = new GetToken(vaultService,applicationProperties, userService);
    logger.debug("url : " + url.toString());

    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "application/json");

    conn.setRequestProperty("Authorization", "bearer " + getToken.gettingToken(request.getHeader("userName")));
    String properties="";
    byte[] properties1 = properties.getBytes(StandardCharsets.UTF_8);

    OutputStream stream = conn.getOutputStream();

    stream.write(properties1);

    logger.debug(conn.getResponseCode() + "-----" + conn.getResponseMessage());


    if (conn.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED || conn.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT ) { //success

      BufferedReader in1 = new BufferedReader(new InputStreamReader(
              conn.getInputStream()));

      while ((inputLine = in1.readLine()) != null) {
        response.append(inputLine);
      }
      logger.debug(response);

      map.put("Status", aksName+" is stopped successfully");

      in1.close();
    }
    conn.disconnect();

    return map;
  }

  @Override
    public List<String> getKubernetVersion(HttpServletRequest request,String resourceGroupName,String aksName) throws JSONException {
    List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

    var secret =
            vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

      logger.info("getting Kubernetes Version is started...");
      List<String> stringList= new ArrayList<>();
      try {

        ProcessBuilder processBuilder = new
                ProcessBuilder("bash", applicationProperties.getAppScriptPath() + "aksupgrade.sh", secret.getClientId(), secret.getClientSecret(), secret.getTenantId(), "version", resourceGroupName, aksName);
        logger.debug("cmdList " + processBuilder.command());

        Process process = new
                ProcessBuilder("bash",
                applicationProperties.getAppScriptPath() + "aksupgrade.sh",
                secret.getClientId(),
                secret.getClientSecret(),
                secret.getTenantId(),
                "version",
                resourceGroupName,
                aksName).start();
        logger.debug("After Process");
        BufferedReader Reader = new BufferedReader(new InputStreamReader(
                process.getErrorStream()));
        if (process.waitFor() != 0) {
          logger.error("Error Reader.readLine() ::" + Reader.readLine());
          throw new AzureAcltrRuntimeException(
                  Reader.readLine(),
                  null,
                  Reader.readLine(),
                  HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
          BufferedReader response = new BufferedReader(new InputStreamReader(
                  process.getInputStream()));
          logger.info("aksUpgrade Script Exc Ended...");
          String line = "";
          while ((line = response.readLine()) != null) {
            logger.info("Reading line for response...");
            stringList.add(line);
          }
          return stringList;
        }
      }catch (Exception e) {
        logger.error("aksUpgrade Exception : "+e.getMessage());
        throw new AzureAcltrRuntimeException(
                e.getMessage(),
                null,
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
      }
  }
  private String integratingAKStoACR(HttpServletRequest request,String resourceGroupName,String aksName,String acr) throws JSONException {

    List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

    var secret =
            vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());


    List<String> stringList = new ArrayList<>();
    try {
      logger.info("applicationProperties.getAppScriptPath() integrateAcr.sh:::::"+"E:\\AZX-FEB-2022-V1\\azure-accelerator-sdk\\scripts\\integrateAcr.sh");


      logger.info("aks Integration Started...");
      ProcessBuilder processBuilder = new
              ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "integrateAcr.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),resourceGroupName,aksName,acr);
      logger.debug("cmdList "+processBuilder.command());
      Process process = new
              ProcessBuilder("bash",
              applicationProperties.getAppScriptPath() + "integrateAcr.sh",
              secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),
              resourceGroupName,aksName,acr).start();
      logger.debug("After Process");
      BufferedReader Reader = new BufferedReader(new InputStreamReader(
              process.getErrorStream()));
      String errorLine = "";
      while ((errorLine = Reader.readLine()) != null) {
        stringList.add(errorLine);
      }
      if (process.waitFor() != 0) {
        throw new AzureAcltrRuntimeException(
                String.join("", stringList),
                null,
                String.join("", stringList),
                HttpStatus.INTERNAL_SERVER_ERROR);
      } else {
        BufferedReader response = new BufferedReader(new InputStreamReader(
                process.getInputStream()));
        logger.info("aks Integration Ended...");
        return String.join("", stringList);
      }
    } catch (Exception e) {
      logger.error("error occurred while integrating the AKS to ACR::::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
              e.getMessage(),
              null,
              e.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR, e);
    }
  }

    private AzureCredentials applicationTokenCredentials(String userName) throws JSONException {
      List<LocalUserDto> userDtoList=userService.findByName(userName);

      var secret =
              vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

    return
        new AzureCredentials(
            new ApplicationTokenCredentials(
                secret.getClientId(), secret.getTenantId(), secret.getClientSecret(),
                AzureEnvironment.AZURE),
            secret.getSubscriptionId());
  }

}
