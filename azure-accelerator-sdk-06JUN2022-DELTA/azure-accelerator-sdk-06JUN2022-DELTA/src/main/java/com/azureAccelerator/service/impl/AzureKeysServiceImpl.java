package com.azureAccelerator.service.impl;

import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.*;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.models.*;
import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.*;

import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.AzureKeysService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONValue;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AzureKeysServiceImpl implements AzureKeysService {

    private static final Logger logger = LogManager.getLogger(AzureKeysServiceImpl.class);
    private final VaultService vaultService;
    private final ApplicationProperties applicationProperties;
    private final UserService userService;




    public AzureKeysServiceImpl(VaultService vaultService, ApplicationProperties applicationProperties, UserService userService) {
        this.vaultService = vaultService;
        this.applicationProperties = applicationProperties;

        this.userService = userService;
    }

    @Override
    public KeysResponseDto createKeys(HttpServletRequest request, KeysDto keysDto) throws Exception {


        KeyVaultKey keyVaultKey=null;
        logger.info("key creating...");


        try{

            ClientSecretCredential clientSecretCredential=getClientSecretCredential(request.getHeader("userName"));

            String keyVaultName = keysDto.getKeyVaultName();
            String keyVaultUri = "https://" + keyVaultName + ".vault.azure.net";
            logger.info("keyVaultUri : "+keyVaultUri);
            KeyType keyType=keysDto.getKeyType();

            KeyClient keyClient = new KeyClientBuilder()
                    .vaultUrl(keyVaultUri)
                    .credential(clientSecretCredential)
                    .buildClient();

            for (KeyProperties keyProperties :keyClient.listPropertiesOfKeys()) {
                logger.debug(">>>>>>>>>"+keyProperties.getName());
                if(keyProperties.getName().equals(keysDto.getKeyName())){
                    throw new Exception(keysDto.getKeyName()+" is already available");
                }
            }


                logger.debug("KeyClient is : " + keyClient);
                KeyVaultKey keyVaultKey1 = keyClient.createKey(keysDto.getKeyName(), keyType);
                keysDto.setKId(keyVaultKey1.getId());

                logger.debug(keyVaultKey1.getName() + ", " + keyVaultKey1.getId());

            logger.info("key created...");


        } catch (
                CloudException e) {
            logger.error("createKeys Exception : "+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return KeysResponseDto.builder().keyId(keysDto.getKId())
                .keyName(keysDto.getKeyName())
                .keyType(keysDto.getKeyType())
                .build();

    }

    @Override
    public Object getKeys(HttpServletRequest request,String keyVaultName) throws Exception {

        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));
        logger.info("getting all keys...");

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
        //Process process;
        List<String> stringList= new ArrayList<>();
        try {

            List<String> cmdList = new ArrayList<>();
            cmdList.add("bash");
            cmdList.add(applicationProperties.getAppScriptPath()+"listKeyVaultKeys.sh");
            cmdList.add(secret.getClientId());
            cmdList.add(secret.getClientSecret());
            cmdList.add(secret.getTenantId());
            cmdList.add(keyVaultName);



            String cmd = "bash "+applicationProperties.getAppScriptPath() + "listKeyVaultKeys.sh";

            ProcessBuilder processBuilder = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "listKeyVaultKeys.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),keyVaultName);
            logger.debug("cmdList "+processBuilder.command());

            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "listKeyVaultKeys.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),keyVaultName).start();
            logger.info("After Process");

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
                logger.info("Reading line for response..."+stringList);
                org.json.simple.JSONArray obj = (org.json.simple.JSONArray) JSONValue.parse(stringList.toString());
                return obj.get(0);
            }
        } catch (Exception e) {
            logger.error("Exception : list all keys : "+ e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    @Override
    public Map<String,String> deleteKey(HttpServletRequest request,String keyName,String keyVaultName) throws JSONException {
        Map<String,String> map =new HashMap<>();
        logger.info("deleting key...");

        ClientSecretCredential clientSecretCredential=getClientSecretCredential(request.getHeader("userName"));

        String keyVaultUri = "https://" + keyVaultName + ".vault.azure.net";
        logger.debug("KeyVault Url : "+keyVaultUri);
        try{
             KeyClient keyClient = new KeyClientBuilder()
                     .vaultUrl(keyVaultUri)
                     .credential(clientSecretCredential)
                     .buildClient();

             SyncPoller<DeletedKey, Void> deletedKeyPoller = keyClient.beginDeleteKey(keyName);

             PollResponse<DeletedKey> deletedKeyPollResponse = deletedKeyPoller.poll();


             DeletedKey deletedKey = deletedKeyPollResponse.getValue();
             logger.debug(deletedKey);
             logger.info("Deletion is successfully" );

              deletedKeyPoller.waitForCompletion();
              map.put("status", keyName+" : key is deleted successfully");
              logger.debug(map);
              logger.info("deleting key is ended");
        } catch (CloudException e) {
            logger.error("delete key Exception : "+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return map;
    }

    @Override
    public String importKey(ImportKeyDto importKey)throws Exception{



        return importKey.getKeyName()+" is imported successfully.";
    }

   private ClientSecretCredential getClientSecretCredential(String userName) throws JSONException {
       List<LocalUserDto> userDtoList=userService.findByName(userName);

       var keys =
               vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());


        return new ClientSecretCredentialBuilder().clientId(keys.getClientId()).clientSecret(keys.getClientSecret())
                .tenantId(keys.getTenantId()).build();
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
