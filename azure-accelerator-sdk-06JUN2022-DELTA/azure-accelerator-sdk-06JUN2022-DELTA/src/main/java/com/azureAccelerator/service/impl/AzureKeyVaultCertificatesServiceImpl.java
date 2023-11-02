package com.azureAccelerator.service.impl;

import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.*;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.AzureKeyVaultCertificatesService;
import com.azureAccelerator.service.IntegratingAKStoACRService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.azureAccelerator.util.GetToken;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AzureKeyVaultCertificatesServiceImpl implements AzureKeyVaultCertificatesService {

    private static final Logger logger = LogManager.getLogger(AzureKeyVaultCertificatesServiceImpl.class);
    private final VaultService vaultService;
    private final ApplicationProperties applicationProperties;
    private final IntegratingAKStoACRService integratingAKStoACRService;
    private final UserService userService;


    public AzureKeyVaultCertificatesServiceImpl(VaultService vaultService, ApplicationProperties applicationProperties, IntegratingAKStoACRService integratingAKStoACRService, UserService userService) {
        this.vaultService = vaultService;
        this.applicationProperties = applicationProperties;
        this.integratingAKStoACRService = integratingAKStoACRService;
        this.userService = userService;
    }


    @Override
    public KeyVaultCertificatesResponseDto createCertificate(HttpServletRequest request, KeyVaultCertificatesDto keyVaultCertificatesDto) {

        return null;
    }

    @Override
    public List<KeyVaultCertificatesResponseDto> listCertificate(HttpServletRequest request, String vaultName) throws JSONException {

        KeyVaultCertificate certificate;
        Map<String,String> map=new HashMap<>();
        List<KeyVaultCertificatesResponseDto> keyVaultCertificatesResponseDtos=new ArrayList<>();
        logger.info("getting all certificates...");
        try{

            String keyVaulturl="https://"+vaultName+".vault.azure.net/";
            logger.debug(keyVaulturl);
            ClientSecretCredential credential=getClientSecretCredential(request.getHeader("userName"));
            CertificateClient certificateClient = new CertificateClientBuilder()
                    .vaultUrl(keyVaulturl)
                    .credential(credential).buildClient();

            for (CertificateProperties certificateProperties : certificateClient.listPropertiesOfCertificates()) {
                KeyVaultCertificate certificateWithAllProperties =
                        certificateClient.getCertificateVersion(certificateProperties.getName(), certificateProperties.getVersion());
                logger.debug("certificate Name :"+certificateProperties.getName());
                logger.debug("certificate version :"+certificateProperties.getVersion());
                map.put(certificateWithAllProperties.getProperties().getName(),certificateWithAllProperties.getSecretId());
                keyVaultCertificatesResponseDtos.add(KeyVaultCertificatesResponseDto.builder()
                        .CertificateName(certificateWithAllProperties.getName())
                                .CertificateId(certificateWithAllProperties.getSecretId())
                        .build());

                logger.info("Received certificate with name \"%s\" and secret id %s"+
                        certificateWithAllProperties.getProperties().getName()+ certificateWithAllProperties.getSecretId());

            }
            logger.info("getting all certifications ended...");
        } catch (
                CloudException e) {
            logger.error("listCertificate Exception : "+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return keyVaultCertificatesResponseDtos;


    }

    @Override
    public Map<String, String> deleteCertificate(HttpServletRequest request,String keyVaultName,String certificateName) throws JSONException {

        KeyVaultCertificate certificate;
        Map<String,String> map=new HashMap<>();
        logger.info("certificate deleting...");

        try{

            String keyVaulturl="https://"+keyVaultName+".vault.azure.net/";

            ClientSecretCredential credential=getClientSecretCredential(request.getHeader("userName"));


            CertificateClient certificateClient = new CertificateClientBuilder()
                    .vaultUrl(keyVaulturl)
                    .credential(credential).buildClient();

            SyncPoller<DeletedCertificate, Void> deleteCertificatePoller =
                    certificateClient.beginDeleteCertificate(certificateName);

            PollResponse<DeletedCertificate> pollResponse = deleteCertificatePoller.poll();

            logger.debug("Deleted certificate with name \"%s\" and recovery id %s"+ pollResponse.getValue().getName()+
                    pollResponse.getValue().getRecoveryId());

            deleteCertificatePoller.waitForCompletion();
            map.put("status",certificateName+" deleted Successfully");
            logger.info("certificate deleting is ended...");
        } catch (
                CloudException e) {
            logger.error("deleteCertificate Exception : "+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return map;
    }

    @Override
    public String createCertificate2(HttpServletRequest request, PolicyDto policyDto) {
        logger.info("certificate creating...");

        String output=null;
        String inputLine=null;
        StringBuffer response = new StringBuffer();

        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));

            URL url = new URL("https://"+policyDto.getKeyvaultName()+".vault.azure.net/certificates/"+policyDto.getCertificateName()+"/create?api-version=7.2");

            GetToken getToken = new GetToken(vaultService, applicationProperties, userService);
            logger.debug("url : " + url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "bearer "+getToken.gettingVaultToken(request.getHeader("userName")));

            logger.debug("policy details :"+policyDto.toString());
            String properties = "{\n" +
                    "  \"policy\": {\n" +
                    "    \"x509_props\": {\n" +
                    "      \"subject\": \"CN="+policyDto.getSubject()+"\"\n" +
                    "      },\n" +
                    "    \"issuer\": {\n" +
                    "      \"name\": \""+policyDto.getIssuer2()+"\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            logger.debug("policy::::"+properties);


            byte[] properties1 = properties.getBytes(StandardCharsets.UTF_8);

            OutputStream stream = conn.getOutputStream();

            stream.write(properties1);

            logger.debug(conn.getResponseCode() + "-----" + conn.getResponseMessage());
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK ||conn.getResponseCode() == HttpURLConnection.HTTP_CREATED||conn.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED) { //success

                BufferedReader in1 = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));

                while ((inputLine = in1.readLine()) != null) {
                    response.append(inputLine);
                }
                in1.close();
            }
        }catch (Exception e) {
            logger.error("createCertificate Exception ; "+e.getMessage());
        }
        return response.toString();
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
