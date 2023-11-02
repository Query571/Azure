package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.AzureCommonResponseDto;
import com.azureAccelerator.dto.AzureCredentials;
import com.azureAccelerator.dto.ExportTempDto;
import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.AzureTemplateService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentMode;
import com.microsoft.azure.management.resources.ResourceGroupExportTemplateOptions;
import com.microsoft.azure.management.resources.ResourceReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AzureTemplateServiceImpl implements AzureTemplateService {

  private static final Logger logger = LogManager.getLogger(AzureTemplateServiceImpl.class);
  private final ApplicationProperties applicationProperties;
  private final VaultService vaultService;
  private final UserService userService;


  public AzureTemplateServiceImpl(ApplicationProperties applicationProperties,
                                  VaultService vaultService, UserService userService) {
    this.applicationProperties = applicationProperties;
    this.vaultService = vaultService;
    this.userService = userService;
  }

  @Override
  public AzureCommonResponseDto exportTemplate(HttpServletRequest request, ExportTempDto exportTempDto) throws JSONException {

    List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

    var secret =
            vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

    Process process;
    try {
      logger.info("Script Exc Started...");
      List<String> cmdList = new ArrayList<>();
      cmdList.add("bash");
      cmdList.add(applicationProperties.getAppScriptPath()+"exportTemplate.sh");
      cmdList.add(secret.getClientId());
      cmdList.add(secret.getClientSecret());
      cmdList.add(secret.getTenantId());
      cmdList.add(exportTempDto.getResourceGroupName());
      cmdList.add(exportTempDto.getResourceName());
      cmdList.addAll(exportTempDto.getResourceId());

      process = new ProcessBuilder(cmdList).start();
     /* Process process = new
              ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "exportTemplate.sh",
              secret.getClientId(),
              secret.getClientSecret(),
              secret.getTenantId(),
              exportTempDto.getResourceGroupName(),
              exportTempDto.getResourceName(),exportTempDto.getResourceId()).start();*/
      BufferedReader Reader = new BufferedReader(new InputStreamReader(
          process.getErrorStream()));
      logger.debug("Status Code :"+process.waitFor());
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
      }else {
        BufferedReader response = new BufferedReader(new InputStreamReader(
            process.getInputStream()));
        logger.debug("Export Template of Response:"+response.readLine());
        logger.info("Script Exc Ended...");
        return AzureCommonResponseDto.builder().name(response.readLine()).build();
      }
    } catch (Exception e) {
      logger.error("Error occurred while fetching exportTemplate::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.getMessage(),
          null,
          e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public String exportAllTemplates(HttpServletRequest request,String resourceGroupName) throws JSONException {

    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure()
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());
      logger.info("Export AllTemplates is starting...");
      String templateJson = azure
          .resourceGroups()
          .getByName(resourceGroupName)
          .exportTemplate(ResourceGroupExportTemplateOptions.INCLUDE_BOTH)
          .templateJson();
      logger.debug("templateJson ::"+templateJson);
      logger.debug("=========================================");
      logger.info("Export AllTemplates is ended...");
    } catch (CloudException e) {
      logger.error("Error occurred while getting exportAllTemplates::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return null;
  }

  @Override
  public AzureCommonResponseDto uploadTemplate(HttpServletRequest request,MultipartFile multipartFile, String resourceGroup) throws JSONException {

    long timestamp = new Date().getTime();

    try {
      byte[] readFile = multipartFile.getInputStream().readAllBytes();

      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure()
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());

      logger.info("upload starting");

      Deployment deploy = azure
          .deployments()
          .define("AZX-Deploy-"+timestamp)
          .withExistingResourceGroup(resourceGroup)
          .withTemplate(new String(readFile))
          .withParameters("{}")
          .withMode(DeploymentMode.INCREMENTAL)
          .beginCreate();

      logger.debug("deploy name::"+deploy.name());

      List<String> resources = null;
      for (; ; ) {
        Thread.sleep(10000);
        logger.info("waiting..");
        String provisioningState = azure.deployments().getByName(deploy.name()).provisioningState();
        logger.info("deploy.provisioningState() ::"+provisioningState);
        if(provisioningState.equalsIgnoreCase("Succeeded")){
          logger.debug("provisioningState "+provisioningState);
           resources = azure.deployments().getByName(deploy.name()).outputResources()
              .stream().map(
                  ResourceReference::id).collect(Collectors.toList());

           List<String> resourceNames = new ArrayList<>();
           resources.forEach(resourceId -> {
            String resource = resourceId.substring(resourceId.lastIndexOf("/") + 1);
            logger.debug("resource ::"+resourceId);
             resourceNames.add(resource);
           });
           logger.debug("Resource Names :"+resourceNames);
          logger.info("resourceNames ::"+resourceNames+" is successfully created.");
          return AzureCommonResponseDto.builder().name(resourceNames +" are successfully created.").build();
        } else if(provisioningState.equalsIgnoreCase("Failed")){
          logger.debug("provisioningState "+provisioningState);
          throw new AzureAcltrRuntimeException(
              "Deployment is "+provisioningState,
              null,
              "Deployment is "+provisioningState,
              HttpStatus.BAD_REQUEST);
        }
      }
    } catch (CloudException e) {
      logger.error("Error occurred while getting uploadTemplate::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }catch ( IOException | InterruptedException e){
      logger.error("Error occurred while getting uploadTemplate::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
              e.getMessage(),
              null,
              e.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR);
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
