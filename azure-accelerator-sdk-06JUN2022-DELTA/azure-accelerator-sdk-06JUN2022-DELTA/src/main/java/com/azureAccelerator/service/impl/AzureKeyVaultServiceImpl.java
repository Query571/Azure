package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.AzureCredentials;
import com.azureAccelerator.dto.KeyVaultDto;
import com.azureAccelerator.dto.KeyVaultResponseDto;
import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.AzureKeyVaultService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.keyvault.CertificatePermissions;
import com.microsoft.azure.management.keyvault.KeyPermissions;
import com.microsoft.azure.management.keyvault.SecretPermissions;
import com.microsoft.azure.management.keyvault.Vault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class AzureKeyVaultServiceImpl implements AzureKeyVaultService {

  private static final Logger logger = LogManager.getLogger(AzureKeyVaultServiceImpl.class);
  private final VaultService vaultService;
  private final ApplicationProperties applicationProperties;
  private final UserService userService;

  public AzureKeyVaultServiceImpl(VaultService vaultService,
                                  ApplicationProperties applicationProperties, UserService userService) {
    this.vaultService = vaultService;
    this.applicationProperties = applicationProperties;
    this.userService = userService;
  }

  @Override
  public KeyVaultResponseDto createKeyVault(HttpServletRequest request,KeyVaultDto keyVaultDto) throws JSONException {
    List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

    var secret =
            vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

    Vault vault = null;
    Map<String,String> tags=new HashMap<>();
    tags.put("resourceGroupName",keyVaultDto.getResourceGroupName());
    tags.put("Name",keyVaultDto.getName());
    tags.put("application","azx");
    tags.put("Environment","dev");
    tags.put("project","cloud");
    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure() // Initial an Azure.Configurable object
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());
      logger.info("Key Vault creation is started...");
      vault = azure
              .vaults()
              .define(keyVaultDto.getName())
              .withRegion(keyVaultDto.getLocation())
              .withExistingResourceGroup(keyVaultDto.getResourceGroupName())
              .defineAccessPolicy()/*.forObjectId("e6a2f7a1-7088-4a54-8c8a-a1c0808331eb")*/
              .forObjectId(secret.getObjectId())
              .allowKeyPermissions(KeyPermissions.GET,KeyPermissions.LIST,KeyPermissions.UPDATE,KeyPermissions.CREATE,KeyPermissions.IMPORT,KeyPermissions.DELETE)
              .allowSecretPermissions(SecretPermissions.GET,SecretPermissions.LIST,SecretPermissions.SET,SecretPermissions.DELETE)
              .allowCertificatePermissions(CertificatePermissions.GET,CertificatePermissions.LIST,CertificatePermissions.UPDATE,CertificatePermissions.CREATE,CertificatePermissions.IMPORT,CertificatePermissions.DELETE,CertificatePermissions.LISTISSUERS,CertificatePermissions.GETISSUERS,CertificatePermissions.SETISSUERS,CertificatePermissions.MANAGEISSUERS)
              .attach().withTags(tags)
              .create();

      logger.debug("vault.id() ::" + vault.id());
      logger.debug("vault.vaultURI ::" + vault.vaultUri());
      logger.info("vault.name ::" + vault.name());
      logger.info("Key Vault creation is ended...");
    } catch (CloudException e) {
      logger.error("Exception : createKeyVault : "+ e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return
        KeyVaultResponseDto
            .builder()
            .id(vault.id())
            .name(vault.name())
            .vaultURI(vault.vaultUri())
            .location(vault.regionName())
            .tags(vault.tags())
            .build();
  }

  @Override
  public String deleteKeyVault(HttpServletRequest request,String keyVaultId) throws Exception {
    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure() // Initial an Azure.Configurable object
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());
      logger.info("deleting KeyVault...");
      Vault vault=azure.vaults().getById(keyVaultId);
      logger.debug("vault ID"+vault.id());
      logger.debug("vault URI :"+vault.vaultUri());
      try {
        azure.vaults().deleteById(keyVaultId);
      }catch (Exception e){
        logger.error("Exception : deleteKeyVault : "+ e.getMessage());
        throw new Exception(keyVaultId+" Details  Not found");
      }
      logger.info("keyVault deleted");
    } catch (CloudException e) {
      logger.error("Exception : deleteKeyVault : "+ e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return null;
  }

  @Override
  public List<KeyVaultResponseDto> getKeyVault(HttpServletRequest request, String resourceGroupName) throws JSONException {
    List<KeyVaultResponseDto> keyVaultResponseList = new ArrayList<>();

    try {
      AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
      Azure azure = Azure.configure() // Initial an Azure.Configurable object
          .authenticate(credentials.getApplicationTokenCredentials())
          .withSubscription(credentials.getSubscriptionId());
      logger.info("listing all keyVaults are started...");
      ArrayList<Vault> keyVaultList = new ArrayList<>(
          azure.vaults().listByResourceGroup(resourceGroupName));
      logger.debug(keyVaultList);
      keyVaultList.forEach(vault -> {
        keyVaultResponseList
            .add(KeyVaultResponseDto
                .builder()
                .id(vault.id())
                .name(vault.name())
                .vaultURI(vault.vaultUri())
                .location(vault.regionName())
                .tags(vault.tags())
                .build());
        logger.debug("vault ID"+vault.id());
        logger.debug("vault URI :"+vault.vaultUri());
      });
      logger.debug("keyVaultResponseList :"+keyVaultResponseList);
    } catch (CloudException e) {
      logger.error("Exception : getKeyVault : "+ e.getMessage());
      throw new AzureAcltrRuntimeException(
          e.body().message(),
          null,
          e.body().message(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return keyVaultResponseList;
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
