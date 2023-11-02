package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.AKSContainerRegistryResponseDto;
import com.azureAccelerator.dto.AzureCredentials;
import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.AKSContainerRegistryService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.containerregistry.Registry;
//import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONValue;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class AKSContainerRegistryServiceImpl implements AKSContainerRegistryService {

    private static final Logger logger = LogManager.getLogger(AKSContainerRegistryServiceImpl.class);
    private final VaultService vaultService;
    private final ApplicationProperties applicationProperties;
    private final UserService userService;

    public AKSContainerRegistryServiceImpl(VaultService vaultService,
                                           ApplicationProperties applicationProperties, UserService userService) {
        this.vaultService = vaultService;
        this.applicationProperties = applicationProperties;
        this.userService = userService;
    }

    @Override
    public List<AKSContainerRegistryResponseDto> getContainerRegistry(HttpServletRequest request, String resourceGroupName) throws JSONException {
        try {
             logger.info(" aks container registry begins");
            List<AKSContainerRegistryResponseDto> aksContainerRegistryResponseDtoList= new ArrayList<>();
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure()
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            logger.debug(azure);
            PagedList<Registry> registryPagedList = azure.containerRegistries().listByResourceGroup(resourceGroupName);
            logger.debug(registryPagedList);
            registryPagedList.forEach(registry -> {
                AKSContainerRegistryResponseDto aksContainerRegistryResponseDto=
                        AKSContainerRegistryResponseDto.builder().
                                name(registry.name())
                                .loginServerUrl(registry.loginServerUrl())
                                .location(registry.regionName())
                                .creationDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(registry.creationDate().toDate()))
                                .tags(registry.tags())
                                .build();
                aksContainerRegistryResponseDtoList.add(aksContainerRegistryResponseDto);

            });
            logger.debug(registryPagedList);
            logger.info(" aks container registration done");
            return aksContainerRegistryResponseDtoList;

        }catch (CloudException e){
            logger.error("Error occured while getting container registry  :::::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR
);
        }

    }
    @Override
    public AKSContainerRegistryResponseDto createConainerReg(HttpServletRequest request,String name, String resourceGroupName,String region,String acrSize) throws JSONException {

        Map<String,String> tags=new HashMap<>();
        tags.put("resourceGroupName",resourceGroupName);
        tags.put("name",name);
        tags.put("application","azx");
        tags.put("Environment","dev");
        tags.put("project","cloud");

        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        Azure azure = Azure.configure()
                .authenticate(credentials.getApplicationTokenCredentials())
                .withSubscription(credentials.getSubscriptionId());
        logger.debug(azure);
        Registry registry = null;
        try {
            logger.info("acr creation start...");

        if(acrSize.equalsIgnoreCase("Basic")){
            logger.debug("Basic Registry creation");
            registry = azure.containerRegistries()
                    .define(name)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroupName)
                    .withBasicSku()
                    .withRegistryNameAsAdminUser()
                    .withTags(tags)
                    .create();

        }else if (acrSize.equalsIgnoreCase("Standard")){
            logger.debug("Standard Registry creation");
            registry = azure.containerRegistries()
                    .define(name)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroupName)
                    .withStandardSku()
                    .withRegistryNameAsAdminUser()
                    .withTags(tags)
                    .create();
        }else if(acrSize.equalsIgnoreCase("Premium")){
            logger.debug("Premium Registry creation");
            registry = azure.containerRegistries()
                    .define(name)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroupName)
                    .withPremiumSku()
                    .withRegistryNameAsAdminUser()
                    .withTags(tags)
                    .create();
        }
        logger.info("acr creation is successful...");
        } catch (CloudException e) {
            logger.error("Error occurred while acr creation ::: "+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }



        return AKSContainerRegistryResponseDto.builder()
                .name(registry.name())
                .loginServerUrl(registry.loginServerUrl())
                .location(registry.regionName())
                .tags(registry.tags())
                .creationDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(registry.creationDate().toDate()))
                .build();
    }

    @Override
    public List<String> getContainerRepository(HttpServletRequest request,String containerRegName) throws JSONException {

        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
        logger.info("Container Repository displaying List started...");

        List<String> stringList= new ArrayList<>();
     try {

            ProcessBuilder processBuilder = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "acrRepo.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),containerRegName);
         logger.debug("cmdList "+processBuilder.command());

         Process process = new
                 ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "acrRepo.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),containerRegName).start();
            logger.debug("commands to pass args :ClientId,ClientSecret, TenantId, "+containerRegName);

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
             logger.info("Repository listing Ended.....");
             String line="";
             while ((line=response.readLine())!=null) {
                 stringList.add(line);
             }
             return stringList;
         }
        } catch (IOException | InterruptedException e) {
         logger.error("Error occurred while fetching container registry details::::"+e.getMessage());
        }
        return stringList;

    }


    @Override
    public Map<String, String> deleteAcr(HttpServletRequest request,String resourceGroupName, String registryName) throws Exception {

        Map<String, String> map=new HashMap<>();
        try{
        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        Azure azure = Azure.configure()
                .authenticate(credentials.getApplicationTokenCredentials())
                .withSubscription(credentials.getSubscriptionId());
        logger.info("acr deletion is started...");
        String acrId="/subscriptions/"+credentials.getSubscriptionId()+"/resourceGroups/"+resourceGroupName+"/providers/Microsoft.ContainerRegistry/registries/"+registryName;
           Registry registry= azure.containerRegistries().getById(acrId);
        if (registry!=null) {
            azure.containerRegistries().deleteById(acrId);
            logger.debug(registry.name()+" is deleting");
        }else{
            logger.error(acrId +" details are not found");
            throw new Exception(acrId +" details are not found");
        }
        map.put("status",registryName+" is deleted.");
        logger.info("acr deletion is successful");
    } catch (CloudException e) {
            logger.error("Error occured while delete Acr "+e.getMessage());
        throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

        return map;
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
