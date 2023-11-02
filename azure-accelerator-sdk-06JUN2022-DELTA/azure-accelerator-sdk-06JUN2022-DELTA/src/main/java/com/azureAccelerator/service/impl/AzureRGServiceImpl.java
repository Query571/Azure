package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.AzureCommonResponseDto;
import com.azureAccelerator.dto.AzureCredentials;
import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.repository.AzureInstanceTypesRepository;
import com.azureAccelerator.service.AzureRGService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AzureRGServiceImpl implements AzureRGService {

   private static final Logger logger = LogManager.getLogger(AzureRGServiceImpl.class);
    private final VaultService vaultService;
    private final ApplicationProperties applicationProperties;
    private final AzureInstanceTypesRepository azureInstanceTypesRepository;
    private final UserService userService;


    @Autowired
    public AzureRGServiceImpl(VaultService vaultService,
                              ApplicationProperties applicationProperties,
                              AzureInstanceTypesRepository azureInstanceTypesRepository, UserService userService) {
        this.vaultService = vaultService;
        this.applicationProperties = applicationProperties;
        this.azureInstanceTypesRepository = azureInstanceTypesRepository;
        this.userService = userService;
    }

    @Override
    public AzureCommonResponseDto createResourceGrp(HttpServletRequest request, String resourceGroupName, String rigonName) throws JSONException {
        Map<String,String> tags=new HashMap<>();
        tags.put("resourceGroupName",resourceGroupName);
        tags.put("regionName",rigonName);
        tags.put("application","azx");
        tags.put("Environment","dev");
        tags.put("project","cloud");
        logger.info("creating ResourceGroup...");
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure()
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            ResourceGroup resourceGroup = azure.resourceGroups()
                    .define(resourceGroupName)
                    .withRegion(rigonName)
                    .withTags(tags)
                    .create();
            logger.debug("ResourceGroupId ::"+resourceGroup.id());
            logger.info("Resource Group is created successfully");
            return AzureCommonResponseDto.builder().name(resourceGroup.name()).tags(resourceGroup.tags()).build();
        } catch (CloudException e) {
            logger.error("Error occurred while createResourceGrp:::::"+e.getMessage());
            if (e.body().code().equalsIgnoreCase("LocationNotAvailableForResourceGroup")) {
                throw new AzureAcltrRuntimeException(
                        "The provided location " +"'"+resourceGroupName+"'" + " is not available for resource group ",
                        null,
                        "The provided location " +"'"+rigonName+"'" + " is not available for resource group ",
                        HttpStatus.BAD_REQUEST);
            } else {
                throw new AzureAcltrRuntimeException(
                        e.body().message(),
                        null,
                        e.body().message(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
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
