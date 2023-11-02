package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.AppRegistrationResponse;
import com.azureAccelerator.dto.AzureCredentials;
import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.AzureAppRegistrationService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryApplication;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class AzureAppRegistrationServiceImpl implements AzureAppRegistrationService {

    private static final Logger logger = LogManager.getLogger(AzureAppRegistrationServiceImpl.class);
    private final VaultService vaultService;
    private final ApplicationProperties applicationProperties;
    private final UserService userService;


    public AzureAppRegistrationServiceImpl(VaultService vaultService,
                                           ApplicationProperties applicationProperties, UserService userService) {
        this.vaultService = vaultService;
        this.applicationProperties = applicationProperties;
        this.userService = userService;
    }


    @Override
    public AppRegistrationResponse createAppReg(HttpServletRequest request, String appName, String appUri) throws JSONException {
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure()
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            ServicePrincipal servicePrincipalExist = azure.accessManagement().servicePrincipals().getByName(appName);
            ActiveDirectoryApplication activeDirectoryApplication = null;
            ServicePrincipal servicePrincipal = null;

            logger.info("App Reg Creation is Starting...");

            String passwordValue = generatingRandomAlphanumericString();
            if (servicePrincipalExist == null) {
                activeDirectoryApplication = azure
                        .accessManagement()
                        .activeDirectoryApplications()
                        .define(appName)
                        .withSignOnUrl(appUri)
                        .definePasswordCredential(appName)
                        .withPasswordValue(passwordValue)
                        .attach()
                        .create();
                logger.debug(activeDirectoryApplication);
                servicePrincipal = azure
                        .accessManagement()
                        .servicePrincipals()
                        .define(appName)
                        .withExistingApplication(activeDirectoryApplication)
                        .definePasswordCredential(appName)
                        .withPasswordValue(passwordValue)
                        .attach()
                        .create();
                logger.debug(servicePrincipal);

            } else {
                logger.info("App Reg Creation is ended");
                return AppRegistrationResponse
                        .builder()
                        .message("Application is already exist with name :" + appName)
                        .build();
            }
            logger.info("App Reg Creation is ended");
            return AppRegistrationResponse
                    .builder()
                    .name(activeDirectoryApplication.name())
                    .id(activeDirectoryApplication.id())
                    .appId(activeDirectoryApplication.applicationId())
                    .clientId(servicePrincipal.applicationId())
                    .clientSecret(passwordValue)
                    .build();

        } catch (CloudException e) {
            logger.error("Error occurred while createAppReg:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<AppRegistrationResponse> getAppReg(HttpServletRequest request) throws JSONException {
        logger.info("getting AppReg...");
        try {
            List<AppRegistrationResponse> appRegistrationResponseList = new ArrayList<>();
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure()
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());

            azure.accessManagement().activeDirectoryApplications().list().forEach(
                    activeDirectoryApplication -> {
                        AppRegistrationResponse appRegistrationResponse = AppRegistrationResponse
                                .builder()
                                .id(activeDirectoryApplication.id())
                                .name(activeDirectoryApplication.name())
                                .appId(activeDirectoryApplication.applicationId())
                                .build();
                        appRegistrationResponseList.add(appRegistrationResponse);
                    });
            logger.debug(appRegistrationResponseList);
            logger.info("getting AppReg is ended");
          return appRegistrationResponseList;
        } catch (CloudException e) {
            logger.error("Error occurred while getAppReg::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String deleteAppReg(HttpServletRequest request,String appRegId) throws JSONException {
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure()
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());

            azure.accessManagement().servicePrincipals().deleteById(appRegId);
            return null;
        } catch (CloudException e) {
            logger.error("Error occurred while deleteAppReg:::::"+e.getMessage());
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
        System.out.println(secret.getClientId());

        return
                new AzureCredentials(
                        new ApplicationTokenCredentials(
                                secret.getClientId(), secret.getTenantId(), secret.getClientSecret(),
                                AzureEnvironment.AZURE),
                        secret.getSubscriptionId());
    }

    private String generatingRandomAlphanumericString(){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 30;
        String generatedString = null;
        try {
            Random random = SecureRandom.getInstanceStrong();
           generatedString = random.ints(leftLimit, rightLimit + 1)
                   .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                   .limit(targetStringLength)
                   .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                   .toString();

           System.out.println(generatedString);

       } catch (NoSuchAlgorithmException e) {
            logger.error("Error occured while generatingRandomAlphanumericString:::"+e.getMessage());
        }
        return generatedString;
    }
}
