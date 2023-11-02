/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.service.impl;

import   com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.dto.SecretsDto;
import com.azureAccelerator.dto.SsoConfig;
import com.azureAccelerator.dto.StoredUserCredStatusDto;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.exception.RecordNotFoundException;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.azureAccelerator.util.VaultUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import lombok.val;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class VaultServiceImpl implements VaultService {
    private static final Logger logger = LogManager.getLogger(VaultServiceImpl.class);
    private static final String EXCEPTION_MSG="Invalid Azure Credentials or keys do not have correct permissions, Please try again";

    private final ApplicationProperties applicationProperties;
    private final VaultUtil vaultUtil;
    private final UserService userService;



    @Autowired
    public VaultServiceImpl(
            ApplicationProperties applicationProperties,
            VaultUtil vaultUtil, UserService userService) {
        this.applicationProperties = applicationProperties;
        this.vaultUtil = vaultUtil;

        this.userService = userService;
    }

    @Override
    public SecretsDto getSecrets(String vaultEndpoint, String vaultToken) {
        logger.info("getSecrets....");
        return vaultUtil.getSecrets(vaultEndpoint, vaultToken);
    }

    @Override
    public void addSecret(SecretsDto secret, String secretName) {

        if ("azure".equals(secretName)) {
            val pathSecretName =
                    applicationProperties.getVaultEndpoint() + "/v1/secret/" + secretName;
            try {
                logger.info("Azure credentials are verifying....");
                ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(secret.getClientId(),
                        secret.getTenantId(), secret.getClientSecret(), AzureEnvironment.AZURE);


                PagedList<ResourceGroup> resourceGroups = Azure.configure() // Initial an Azure.Configurable object
                        .authenticate(credentials)
                        .withSubscription(secret.getSubscriptionId())
                        .resourceGroups()
                        .list();
                logger.debug(resourceGroups);

                if (resourceGroups.size() > 0) {
                    logger.info("Azure credentials verified....");
                    logger.info("Storing Azure Credentials to Vault....");
                    VaultUtil.addSecrets(pathSecretName, applicationProperties.getVaultToken(), secret);
                    logger.info("Azure Credentials stored successfully to Vault....");
                }else {
                    throw new com.microsoft.aad.adal4j.AuthenticationException(
                            EXCEPTION_MSG);
                }
            } catch (Exception e) {
                logger.error("Error occurred while addSecret::::::"+e.getMessage());
                throw new AzureAcltrRuntimeException(
                        EXCEPTION_MSG,
                        null,
                        EXCEPTION_MSG,
                        HttpStatus.UNAUTHORIZED);


            }
        }if("azureList".equals(secretName)) {
            val pathSecretName =
                    applicationProperties.getVaultEndpoint() + "/v1/secret/" + secretName;
            try {
                logger.info("Azure credentials are verifying....");
                ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(secret.getClientId(),
                        secret.getTenantId(), secret.getClientSecret(), AzureEnvironment.AZURE);

                PagedList<ResourceGroup> resourceGroups = Azure.configure() // Initial an Azure.Configurable object
                        .authenticate(credentials).withSubscription(secret.getSubscriptionId()).resourceGroups().list();

                if (!resourceGroups.isEmpty()) {
                    logger.info("Azure credentials verified....");
                    logger.info("Storing Azure Credentials to VaultList....");
                    VaultUtil.addSecretsList(pathSecretName, applicationProperties.getVaultToken(), secret);
                    logger.info("Azure Credentials stored successfully to VaultList....");
                } else {
                    throw new com.microsoft.aad.adal4j.AuthenticationException(EXCEPTION_MSG);
                }
            } catch (Exception e) {
                logger.error("Error occurred while addSecret in List::::::" + e.getMessage());
                throw new AzureAcltrRuntimeException(EXCEPTION_MSG, null, EXCEPTION_MSG, HttpStatus.UNAUTHORIZED);

            }
        }

    }

    @Override
    public Map<String,String> updateSecret(SecretsDto secret, String secretId) {
        Map<String,String> map=new HashMap<>();
        try {
            logger.info("updating Vault List...");

            Object obj = getSecretList();
            val pathSecretName = applicationProperties.getVaultEndpoint() + "/v1/secret/azureList";

            JSONObject jsonObj = new JSONObject(obj.toString());
            JSONArray jsonArr = jsonObj.getJSONArray("secret");
            for (int i = 0; i < jsonArr.length(); i++) {
                if (jsonArr.getJSONObject(i).get("secretId").equals(secretId)) {

                    jsonArr.getJSONObject(i).put("secretId", secret.getSubscriptionId());
                    jsonArr.getJSONObject(i).put("secretDetails", new ObjectMapper().writeValueAsString(secret));
                    jsonArr.getJSONObject(i).put("secretActiveRole", secret.getRolePermission());
                }
            }
            jsonObj.put("secret", jsonArr);

            VaultUtil.storeVaultCredentials(pathSecretName, applicationProperties.getVaultToken(), jsonObj.toString());
            map.put("status","SUCCESS");
            logger.info("updated successfully...");
        } catch (Exception e) {
            logger.error("Error occurred while updateSecret in List::::::" + e.getMessage());
            map.put("status","FAILED");
            return map;

        }
        return map;

    }
    public void addSSOInVault(String clientId,String  tenteId,String redirectUrl) {

        val pathSecretName =
                applicationProperties.getVaultEndpoint() + "/v1/secret/sso";
        try {
            logger.info("Azure credentials are verifying....");
            ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(clientId,
                    tenteId,redirectUrl, AzureEnvironment.AZURE);


            logger.info("Azure credentials verified....");
            logger.info("Storing Azure Credentials to Vault....");
            VaultUtil.addSecretsSSO(pathSecretName, applicationProperties.getVaultToken(), clientId,tenteId,redirectUrl);
            logger.info("Azure Credentials stored successfully to Vault....");
        } catch (Exception e) {
            logger.error("Error occurred while addSecret::::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    EXCEPTION_MSG,
                    null,
                    EXCEPTION_MSG,
                    HttpStatus.UNAUTHORIZED);


        }
    }


    @Override
    public Map<String,String>     updateSSO(String clientId,String  tenteId,String redirectUrl){
        Map<String,String> map=new HashMap<>();
        try {
            logger.info("updating Vault List...");

            val pathSecretName = applicationProperties.getVaultEndpoint() + "/v1/secret/sso";

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("clientId", clientId);
            jsonObj.put("tenantId", tenteId);
            jsonObj.put("redirectUrl", redirectUrl);

            VaultUtil.storeVaultCredentials(pathSecretName, applicationProperties.getVaultToken(), jsonObj.toString());
            map.put("status","SUCCESS");
            ssoConfig();
            logger.info("updated successfully...");
        } catch (Exception e) {
            logger.error("Error occurred while updateSecret in List::::::" + e.getMessage());
            map.put("status","FAILED");
            return map;

        }
        return map;

    }
    @Override
    public Map<String,String> removeSecret(String secretId) {
        Map<String,String> map=new HashMap<>();

        try {
            logger.info("Removing secret...");
            Object obj = getSecretList();
            val pathSecretName = applicationProperties.getVaultEndpoint() + "/v1/secret/azureList";

            JSONObject jsonObj = new JSONObject(obj.toString());
            JSONArray jsonArr = jsonObj.getJSONArray("secret");
            for (int i = 0; i < jsonArr.length(); i++) {
                if (jsonArr.getJSONObject(i).get("secretId").equals(secretId)) {
                    jsonArr.remove(i);
                }
            }
            jsonObj.put("secret", jsonArr);

            VaultUtil.storeVaultCredentials(pathSecretName, applicationProperties.getVaultToken(), jsonObj.toString());
            logger.info("Removed secret...");
        } catch (Exception e) {
            map.put("status","FAILED");
            logger.error("Error occurred while addSecret in List::::::" + e.getMessage());
            return map;

        }
        map.put("status","SUCCESS");
        return map;

    }

    @Override
    public Boolean storedCredStatus() {

        SecretsDto secret = null;
        try {
            secret = vaultUtil.getSecrets(
                    applicationProperties.getVaultAzureSecret(),
                    applicationProperties.getVaultToken());

        } catch (Exception e) {
            logger.error("Error occurred while storedCredStatus:::::"+e.getMessage());
            return false;
        }
        if (secret.getClientId() != null && secret.getClientId().length() > 0
                && secret.getClientSecret() != null && secret.getClientSecret().length() > 0
                && secret.getSubscriptionId() != null && secret.getSubscriptionId().length() > 0
                && secret.getTenantId() != null && secret.getTenantId().length() > 0) {
            return true;
        } else {
            throw new AzureAcltrRuntimeException(
                    "Error reading vault secret,Please enter valid credentials",
                    null,
                    "Error reading vault secret,Please enter valid credentials",
                    HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public StoredUserCredStatusDto storedUserCredStatus(HttpServletRequest request) {
        SecretsDto secret = null;
        logger.info("stored User Cred Status started...");
        StoredUserCredStatusDto storedUserCredStatusDto=new StoredUserCredStatusDto();

        try {
            List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

            secret =
                    getSecrets(userDtoList.get(0).getSubscriptionId());
            logger.debug("subId "+secret);

        } catch (Exception e) {
            logger.error("Error occurred while storedCredStatus:::::"+e.getMessage());
            return null;
        }
        if (secret!=null && secret.getClientId() != null && secret.getClientId().length() > 0
                && secret.getClientSecret() != null && secret.getClientSecret().length() > 0
                && secret.getSubscriptionId() != null && secret.getSubscriptionId().length() > 0
                && secret.getTenantId() != null && secret.getTenantId().length() > 0
                && secret.getObjectId() != null && secret.getObjectId().length() > 0) {



            storedUserCredStatusDto.setStatus(true);
            storedUserCredStatusDto.setSubId(secret.getSubscriptionId());
            logger.info("stored User CredStatus ended");
            return storedUserCredStatusDto;


        } else {
            storedUserCredStatusDto.setStatus(false);
            logger.info("stored User CredStatus ended...");
            return storedUserCredStatusDto;
        }

    }

    @Override
    public Boolean cloudCredStoredStatus(String cloudProvider) {
        logger.info("cloud CredStored Status started...");
        SecretsDto secret = null;
        boolean storedStatus = false;
        if("azure".equals(cloudProvider)){
            try {
                secret = vaultUtil.getSecrets(
                        applicationProperties.getVaultAzureSecret(),
                        applicationProperties.getVaultToken());
            } catch (Exception e) {
                storedStatus = false;
            }
            assert secret != null;
            if (secret.getSubscriptionId() != null && secret.getSubscriptionId().length() > 0
                    && secret.getClientId() != null && secret.getClientId().length() > 0
                    && secret.getClientSecret() != null && secret.getClientSecret().length() > 0
                    && secret.getTenantId() != null && secret.getTenantId().length() > 0) {
                storedStatus = true;
            } else {
                throw new AzureAcltrRuntimeException(
                        "Invalid Credentials,Please enter valid credentials",
                        null,
                        "Invalid Credentials,Please enter valid credentials",
                        HttpStatus.UNAUTHORIZED);
            }
        }
        logger.info("cloud CredStored Status ended");
        return storedStatus;
    }

    @Override
    public SecretsDto getSecret() {
        logger.info("getSecret...");
        return  vaultUtil.getSecrets(
                applicationProperties.getVaultAzureSecret(),
                applicationProperties.getVaultToken());
    }

    @Override
    public Object getSecretList() {
        logger.info("getSecret List...");
        System.out.println("sso ---->"+applicationProperties.getVaultAzureSecret());

        return vaultUtil.getSecretsList(
                applicationProperties.getVaultAzureSecretList(),
                applicationProperties.getVaultToken());
    }


    @Override
    public Object getSSOClientIdStatus() throws JSONException {
        logger.info("getSecret List...");

        SsoConfig  jsonObj = getSSOSecrets(
                applicationProperties.getSsoSecret(),
                applicationProperties.getVaultToken()).getData();
        jsonObj.setSsoStatus(jsonObj.getClientId()==null?false:true);
        return jsonObj;
    }


    public  SecretsDto getSecrets(String subId) throws JSONException {
        logger.info("getSecrets...");
        Object object = getSecretList();

        org.json.JSONObject jsonObj = new org.json.JSONObject(object.toString());
        JSONArray jsonArr = jsonObj.getJSONArray("secret");
        SecretsDto secretsDto=new SecretsDto();

        try {
            for (int i = 0; i < jsonArr.length(); i++) {
                if (jsonArr.getJSONObject(i).get("secretId").equals(subId)) {
                    String secretDetails=jsonArr.getJSONObject(i).get("secretDetails").toString();
                    org.json.JSONObject secretDetailsJson= new org.json.JSONObject(secretDetails);

                    secretsDto.setClientId(secretDetailsJson.get("clientId").toString());
                    secretsDto.setClientSecret(secretDetailsJson.get("clientSecret").toString());
                    secretsDto.setTenantId(secretDetailsJson.get("tenantId").toString());
                    secretsDto.setSubscriptionId(secretDetailsJson.get("subscriptionId").toString());
                    secretsDto.setObjectId(secretDetailsJson.get("objectId").toString());
                }
            }

            return secretsDto;
        } catch (Exception e) {
            throw new RecordNotFoundException("Error reading vault secret,Please enter valid credentials", e);
        }

    }

    private void ssoConfig() throws JSONException {

        List<String> stringList= new ArrayList<>();

        try {
            logger.info("ssoConfig Started...");
            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "ssoConfig.sh")
                    .start();

            logger.info("After Process");

            BufferedReader Reader = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            String errorLine="";
            while ((errorLine=Reader.readLine())!=null) {
                stringList.add(errorLine);
            }
            logger.debug(stringList);
            if (process.waitFor() != 0) {
                throw new AzureAcltrRuntimeException(
                        String.join("",stringList),
                        null,
                        String.join("",stringList),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                BufferedReader response = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));
                logger.info("ssoConfig Ended...");
                String line="";
                while ((line=response.readLine())!=null) {
                    stringList.add(line);
                }
                logger.debug(stringList);

            }
        } catch (Exception e) {
            logger.error("Error occurred while importingDockerImages::::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public SsoConfig getSSOSecrets(String vaultEndpoint, String vaultToken) {
        try {
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.set("X-Vault-Token", vaultToken);
            requestHeaders.setContentType(MediaType.APPLICATION_JSON);
            return new RestTemplate()
                    .exchange(
                            vaultEndpoint,
                            HttpMethod.GET,
                            new HttpEntity<Object>("parameters", requestHeaders),
                            SsoConfig.class)
                    .getBody();
        } catch (Throwable t) {
            throw new RecordNotFoundException("Error reading vault secret,Please enter valid credentials", t);
        }
    }
}
