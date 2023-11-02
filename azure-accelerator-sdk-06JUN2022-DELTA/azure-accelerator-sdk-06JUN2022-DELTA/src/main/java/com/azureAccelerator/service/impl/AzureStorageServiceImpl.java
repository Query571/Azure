package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.*;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.AzureStrorageService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.AccessTier;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountSkuType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AzureStorageServiceImpl implements AzureStrorageService {
    private static final Logger logger = LogManager.getLogger(AzureStorageServiceImpl.class);
    private final ApplicationProperties applicationProperties;
    private final VaultService vaultService;
    private static final String TAGKEY="Application";
    private final UserService userService;


    public AzureStorageServiceImpl(ApplicationProperties applicationProperties,
                                   VaultService vaultService, UserService userService) {
        this.applicationProperties = applicationProperties;
        this.vaultService = vaultService;
        this.userService = userService;
    }

    @Override
    public StrorageResponseDto createStorage(HttpServletRequest request, StroageDto stroageDto) throws JSONException {
        Map<String,String> tags=new HashMap<>();
        tags.put("resourceGroupName",stroageDto.getResourceGroupName());
        tags.put("name",stroageDto.getStorageName());
        tags.put("application","azx");
        tags.put("Environment","dev");
        tags.put("project","cloud");
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials()).withSubscription(credentials.getSubscriptionId());
            logger.info("Azure Storage account with name " + stroageDto.getStorageName() + " creation in progress");

            StorageAccount storageAccount = null;
            if(stroageDto.getStorageKind().equalsIgnoreCase("Storage")) {
                storageAccount = azure.storageAccounts()
                        .define(stroageDto.getStorageName())
                        .withRegion(stroageDto.getLocation())
                        .withExistingResourceGroup(stroageDto.getResourceGroupName())
                        .withGeneralPurposeAccountKind()
                        .withSku(StorageAccountSkuType.STANDARD_RAGRS)
                        .withTags(tags)
                        .create();
                logger.debug("StorageAccount Id :"+storageAccount.id());
                logger.debug("StorageAccount Type :"+storageAccount.type());
            }else if(stroageDto.getStorageKind().equalsIgnoreCase("StorageV2")){
                storageAccount = azure.storageAccounts()
                        .define(stroageDto.getStorageName())
                        .withRegion(stroageDto.getLocation())
                        .withExistingResourceGroup(stroageDto.getResourceGroupName())
                        .withGeneralPurposeAccountKindV2()
                        .withSku(StorageAccountSkuType.STANDARD_RAGRS)
                        .withTags(tags)
                        .create();
                logger.debug("StorageAccount Id :"+storageAccount.id());
                logger.debug("StorageAccount Type :"+storageAccount.type());
            }else if(stroageDto.getStorageKind().equalsIgnoreCase("BlobStorage")){
               storageAccount = azure.storageAccounts()
                        .define(stroageDto.getStorageName())
                        .withRegion(stroageDto.getLocation())
                        .withExistingResourceGroup(stroageDto.getResourceGroupName())
                        .withBlobStorageAccountKind()
                        .withAccessTier(AccessTier.COOL)
                        .withSku(StorageAccountSkuType.STANDARD_RAGRS)
                        .withTags(tags)
                        .create();
                logger.debug("StorageAccount Id :"+storageAccount.id());
                logger.debug("StorageAccount Type :"+storageAccount.type());
            }


            logger.info("Azure Storage account with name " + stroageDto.getStorageName() + " created successfully");

            assert storageAccount != null;

            return StrorageResponseDto.builder()
                    .id(storageAccount.id())
                    .name(storageAccount.name())
                    .storageKind(storageAccount.kind())
                    .location(storageAccount.regionName())
                    .tags(storageAccount.tags())
                    .build();
        }catch (CloudException e) {
            logger.error("Exception createStorage : "+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<StrorageResponseDto> storage(HttpServletRequest request,String resourceGroupName) throws JSONException {
        try {
            List<StrorageResponseDto> strorageResponseDtoList = new ArrayList<>();
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials()).withSubscription(credentials.getSubscriptionId());
            logger.info("Getting all Storages is starting...");
            PagedList<StorageAccount> storageAccountPagedList = azure.storageAccounts()
                    .listByResourceGroup(resourceGroupName);

            storageAccountPagedList.forEach(storageAccount -> {

                StrorageResponseDto strorageResponseDto = StrorageResponseDto.builder()
                        .id(storageAccount.id())
                        .name(storageAccount.name())
                        .location(storageAccount.regionName())
                        .storageKind(storageAccount.kind())
                        .tags(storageAccount.tags())
                        .build();
                strorageResponseDtoList.add(strorageResponseDto);
            });
            logger.debug("All StorageAccount List :"+storageAccountPagedList);
            logger.info("Getting all Storages is ended...");
            return strorageResponseDtoList;
        }catch (CloudException e) {
            logger.error("Error occurred while getting storage:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Map<String,String> deleteStorages(HttpServletRequest request, List<StoragesDto> aksIdsDto) throws JSONException {

        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        Azure azure = Azure.configure() // Initial an Azure.Configurable object
                .authenticate(credentials.getApplicationTokenCredentials()).withSubscription(credentials.getSubscriptionId());
        Map<String,String> map=new HashMap<>();
        try{
        for(StoragesDto strgId:aksIdsDto){
            logger.info("Storage is deleting...");
            azure.storageAccounts().deleteById(strgId.getStorageIds());
            logger.info("Storage is deleted");
        }
        map.put("status","Storages are successfully deleted");
        }catch (CloudException e) {
            logger.error("deleteStorage Exception : "+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return map;
    }


    @Override
    public Map<String,String> deleteStorage(HttpServletRequest request,String storageId) throws Exception {
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials()).withSubscription(credentials.getSubscriptionId());

            logger.info("Storage is deleting...");
            Map<String,String> map=new HashMap<>();
            try {
                azure.storageAccounts().deleteById(storageId);
                logger.debug("StorageAccount Id :"+storageId);
                map.put("status",storageId+" is successfully deleted");
                logger.info("Storage is deleted");
            }catch (Exception e){
                throw new Exception(storageId+" Details  Not found");
            }

            return map;

        }catch (CloudException e) {
            logger.error("deleteStorage Exception : "+e.getMessage());
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
                                secret.getClientId(), secret.getTenantId(), secret.getClientSecret(), AzureEnvironment.AZURE),
                        secret.getSubscriptionId());
    }
}
