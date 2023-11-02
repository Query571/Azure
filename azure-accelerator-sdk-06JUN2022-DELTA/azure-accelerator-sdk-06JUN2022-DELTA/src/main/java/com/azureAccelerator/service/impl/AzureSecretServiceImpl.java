package com.azureAccelerator.service.impl;

import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.AzureSecretDto;
import com.azureAccelerator.dto.AzureSecretResponseDto;
import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.AzureSecretService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.microsoft.azure.CloudException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONValue;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AzureSecretServiceImpl implements AzureSecretService {

    private static final Logger logger = LogManager.getLogger(AzureSecretServiceImpl.class);
    private final VaultService vaultService;
    private final ApplicationProperties applicationProperties;
    private final UserService userService;


    public AzureSecretServiceImpl(VaultService vaultService, ApplicationProperties applicationProperties, UserService userService) {
        this.vaultService = vaultService;
        this.applicationProperties = applicationProperties;
        this.userService = userService;
    }

    @Override
    public AzureSecretResponseDto createSecretKey(HttpServletRequest request, AzureSecretDto azureSecretDto) throws Exception {

        KeyVaultSecret secret = null;
        logger.info("keyVault SecretKey creation is starting...");
        try {
            ClientSecretCredential clientSecretCredential = getClientSecretCredential(request.getHeader("userName"));
            String keyVaultName = azureSecretDto.getVaultName();
            String vaultUri = "https://" + keyVaultName + ".vault.azure.net";
            logger.info("KeyVault URL : "+vaultUri);
            SecretClient secretClient = getSecretClient(clientSecretCredential, vaultUri);

            for (SecretProperties secretProperties:secretClient.listPropertiesOfSecrets()   ) {

                if (secretProperties.getName().equals(azureSecretDto.getSecretKey())) {

                    throw new Exception(azureSecretDto.getSecretKey() + " is already available");
                }
            }


            secret = secretClient.setSecret(azureSecretDto.getSecretKey(), azureSecretDto.getSecretValue());
            logger.debug("Secret : :" + secret.toString());
            logger.debug(secret.getName()+","+secret.getValue());
            logger.info("keyVault SecretKey creation is ended...");
        } catch (CloudException e) {
            logger.error("Exception : create Secret : "+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return AzureSecretResponseDto.builder().id(secret.getId()).secretKey(secret.getName()).secretValue(secret.getValue())
                .build();
    }

    @Override
    public Object getSecret(HttpServletRequest request,String keyVaultName) throws Exception {

        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
        logger.info("getting all Secrets...");
        List<String> stringList= new ArrayList<>();
        org.json.JSONArray jsonArray;

        try {

            ProcessBuilder processBuilder = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "listKeyVaultScrets.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),keyVaultName);
            logger.debug("cmdList "+processBuilder.command());
            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "listKeyVaultScrets.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),keyVaultName).start();
            logger.debug("After Process");
            BufferedReader Reader = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            if (process.waitFor() != 0) {
                logger.error("Error Reader.readLine() ::"+Reader.readLine());
                throw  new Exception(Reader.readLine());
            } else {
                BufferedReader response = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));

                String line="";
                while ((line=response.readLine())!=null) {
                    stringList.add(line);
                }

                org.json.simple.JSONArray obj = (org.json.simple.JSONArray) JSONValue.parse(stringList.toString());
                logger.info("getting all Secrets ended...");
                return obj.get(0);
            }
        } catch (Exception e) {
            logger.error("Error occurred while getSecret::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public Map<String,String> deleteSecret(HttpServletRequest request,String secretName, String keyVaultName) throws JSONException {

        ClientSecretCredential clientSecretCredential = getClientSecretCredential(request.getHeader("userName"));
        Map<String,String> map=new HashMap<>();
        logger.info("secret deletion is started...");
        try{
            String vaultUri = "https://" + keyVaultName + ".vault.azure.net";
            logger.info("KeyVault URL : "+vaultUri);

            SecretClient secretClient = getSecretClient(clientSecretCredential, vaultUri);

            logger.debug("Secret client ::" + secretClient);

            SyncPoller<DeletedSecret, Void> deleteSecretPoller = secretClient.beginDeleteSecret(secretName);
            deleteSecretPoller.waitForCompletion();

            logger.debug("deleted  ::" + deleteSecretPoller);

            PollResponse<DeletedSecret> deletedSecretPollResponse = deleteSecretPoller.poll();

        logger.debug("Deletion date: "+deletedSecretPollResponse.getValue().getDeletedOn());

        map.put("status",secretName+" : deleted Successfully");
            logger.info("secret deletion is ended...");
        } catch (CloudException e) {
            logger.error("Exception : delete secret : "+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return map;
    }

    private ClientSecretCredential getClientSecretCredential(String userName) throws JSONException {
        List<LocalUserDto> userDtoList=userService.findByName(userName);

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
        return new ClientSecretCredentialBuilder().clientId(secret.getClientId()).clientSecret(secret.getClientSecret())
                .tenantId(secret.getTenantId()).build();
    }

    @NotNull
    private SecretClient getSecretClient(ClientSecretCredential clientSecretCredential, String vaultUri) {
        return new SecretClientBuilder().vaultUrl(vaultUri).credential(clientSecretCredential).buildClient();
    }

}
