package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.AzureCredentials;
import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.dto.WebAppDto;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.AzureWebAppService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.WebApp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
public class AzureWebAppServiceImpl implements AzureWebAppService {

  private static final Logger logger = LogManager.getLogger(AzureWebAppServiceImpl.class);
  private final VaultService vaultService;
  private final ApplicationProperties applicationProperties;
  private final UserService userService;


  public AzureWebAppServiceImpl(VaultService vaultService,
                                ApplicationProperties applicationProperties, UserService userService) {
    this.vaultService = vaultService;
    this.applicationProperties = applicationProperties;
    this.userService = userService;
  }

  @Override
  public List<WebAppDto> getAppServices(HttpServletRequest request, String resourceGroupName) throws JSONException {

    List<WebAppDto> webAppDtoList = new ArrayList<>();
    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure()
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());
      logger.info("Getting all Apps starting...");
      PagedList<WebApp> webAppList =
          azure
              .webApps()
              .listByResourceGroup(resourceGroupName);

      webAppList.forEach(webApp -> {
        logger.debug("webApp.id() ::" + webApp.id());
        logger.debug("webApp.name() ::" + webApp.name());
        logger.debug("webApp.operatingSystem() ::" + webApp.operatingSystem());
        logger.debug("webApp.state() ::" + webApp.state());
        logger.debug("webApp.appServicePlanId() ::" + webApp.appServicePlanId());
        logger.debug("webApp.hostNames() ::" + webApp.hostNames());
        AppServicePlan appServicePlan = azure
            .appServices()
            .appServicePlans()
            .getById(webApp.appServicePlanId());

        WebAppDto webAppDto =
            WebAppDto
                .builder()
                .webAppId(webApp.id())
                .webAppName(webApp.name())
                .location(webApp.regionName())
                .status(webApp.state())
                .appServicePlanId(appServicePlan.id())
                .appServicePlanName(appServicePlan.name())
                .operatingSystem(webApp.operatingSystem().toString())
                .hostName(webApp.hostNames())
                .tags(webApp.tags())
                .build();
        webAppDtoList.add(webAppDto);
      });
      logger.info("Getting all Apps ended...");
    } catch (CloudException e) {
      logger.error("Error occurred while fetching getAppServices:::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return webAppDtoList;

  }

  @Override
  public void restartWebApp(HttpServletRequest request,String webAppId) throws JSONException {

    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure()
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());
      logger.info("WebApp restarted starting...");
      azure.
          webApps()
          .getById(webAppId)
          .restart();
      logger.debug("web Id :"+webAppId);
      logger.info("WebApp restarted.");

    } catch (CloudException e){
      logger.error("Error occurred while restartWebApp:::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void startWebApp(HttpServletRequest request,String webAppId) throws JSONException {
    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure()
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());

      logger.info("WebApp starting...");
      azure.
          webApps()
          .getById(webAppId)
          .start();
      logger.debug("web Id :"+webAppId);
      logger.info("WebApp started.");

    } catch (CloudException e){
      logger.error("Error occured while startWebApp:::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void deleteWebApp(HttpServletRequest request,String webAppId) throws JSONException {
    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure()
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());

      logger.info("WebApp deleting starting...");
      azure.
          webApps()
          .deleteById(webAppId);
      logger.debug("web Id :"+webAppId);
      logger.info("WebApp deleted.");

    } catch (CloudException e){
      logger.error("Error occurred while deleteWebApp:::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void stopWebApp(HttpServletRequest request,String webAppId) throws JSONException {

    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure()
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());

      logger.info("WebApp  stopping...");
      azure.
          webApps()
          .getById(webAppId)
          .stop();
      logger.debug("web Id :"+webAppId);
      logger.info("WebApp stopped.");

    } catch (CloudException e){
      logger.error("Error occurred while stopWebApp:::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public List<String> browseWebApp(HttpServletRequest request,String webAppId) throws JSONException {

    List<String> urls = new ArrayList<>();

    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure()
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());

      logger.info("browse WebApp...");
          azure.
              webApps()
              .getById(webAppId)
              .hostNames()
              .forEach(hostName -> {
                String url = "https://"+hostName;
                urls.add(url);
          });
          logger.debug("URLs :"+urls);
          logger.info("browse WebApp ended...");
          return urls;

    } catch (CloudException e) {
      logger.error("Error occurred while browseWebApp::::::"+e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
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
