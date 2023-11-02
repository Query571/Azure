package com.azureAccelerator.service.impl;


import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.AzureCredentials;
import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.dto.SSHKeyDto;
import com.azureAccelerator.service.AKSDeployService;
import com.azureAccelerator.service.SSHKeyClientService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.azureAccelerator.util.GetToken;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class SSHKeyClientServiceImpl implements SSHKeyClientService {
    private static final Logger logger = LogManager.getLogger(SSHKeyClientServiceImpl.class);
    private final VaultService vaultService;
    private final ApplicationProperties applicationProperties;
    private AKSDeployService aksDeployService;
    private final UserService userService;

    public SSHKeyClientServiceImpl(VaultService vaultService, ApplicationProperties applicationProperties, AKSDeployService aksDeployService, UserService userService) {
        this.vaultService = vaultService;
        this.applicationProperties = applicationProperties;
        this.aksDeployService = aksDeployService;
        this.userService = userService;
    }

    @Override
    public Map<String,String> createSSHKey(HttpServletRequest request, SSHKeyDto sshKeyDto) throws Exception {

        String output=null;
        String inputLine=null;
        StringBuffer response = new StringBuffer();
        List<String> sshkeys=gettingSshKeys(request,sshKeyDto.getResourceGroupName());
        for (String sshkey:sshkeys) {
            if(sshKeyDto.getSshPublicKeyName().equals(sshkey)){
                throw new Exception("SSH Key name is already present. Please, Try unique name");
            }
        }
        logger.info("creating ssh key...");

        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));

        URL url=new URL("https://management.azure.com"+
                             "/subscriptions/" +credentials.getSubscriptionId()+
                             "/resourceGroups/"+sshKeyDto.getResourceGroupName()+"/providers/Microsoft.Compute" +
                             "/sshPublicKeys/"+sshKeyDto.getSshPublicKeyName()+"?api-version=2021-11-01");

        GetToken getToken = new GetToken(vaultService,applicationProperties, userService);
        logger.debug("url : " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        //conn.setRequestProperty("location",sshKeyDto.getLocation());
        conn.setRequestProperty("Authorization","bearer "+getToken.gettingToken(request.getHeader("userName")));

        String data = "{\"location\": \""+aksDeployService.getRGLocation(request,sshKeyDto.getResourceGroupName())+"\"}";
        logger.debug(data);
        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = conn.getOutputStream();
        stream.write(out);

        logger.debug(conn.getResponseCode() + "-----" + conn.getResponseMessage());

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { //success

            BufferedReader in1 = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));

            while ((inputLine = in1.readLine()) != null) {
                response.append(inputLine);
            }
            logger.debug(response);

            in1.close();
        }
        conn.disconnect();
        String s=generateKeyPair(request,credentials.getSubscriptionId()
                ,sshKeyDto.getResourceGroupName(),sshKeyDto.getSshPublicKeyName(),aksDeployService.getRGLocation(request,sshKeyDto.getResourceGroupName()));
        Object obj = JSONValue.parse(s.toString());
        JSONObject jsonObject = (JSONObject) obj;
        String privateKey=(String) jsonObject.get("privateKey");
        Map<String,String> privateKeyPair=new HashMap<>();
        privateKeyPair.put("privateKey",privateKey);
        logger.info("ssh key is created");
        return privateKeyPair;

    }

    @Override
    public String getSSHKey(HttpServletRequest request,String resourceGroupName,String sshPublicKeyName) throws Exception {
        String output = null;
        String inputLine = null;
        StringBuffer response = new StringBuffer();
        String sshKeys = "";

        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));

        URL url = new URL("https://management.azure.com" +
                "/subscriptions/" + credentials.getSubscriptionId() +
                "/resourceGroups/" +resourceGroupName+ "/providers/Microsoft.Compute" +
                "/sshPublicKeys/" +sshPublicKeyName+ "?api-version=2021-11-01");

        GetToken getToken = new GetToken(vaultService, applicationProperties, userService);
        logger.info("url : " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "bearer " + getToken.gettingToken(request.getHeader("userName")));


        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in1 = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));


            while ((inputLine = in1.readLine()) != null) {
                response.append(inputLine);

            }
            logger.debug("response :" + response.toString());
                in1.close();
            } else{
                logger.error("GET request not worked");
            }

            conn.disconnect();
            try {
                org.json.JSONObject jsonObject = new org.json.JSONObject(response.toString());
                sshKeys = jsonObject.getJSONObject("properties").getString("publicKey");
            }catch (Exception e){
                logger.error("Exception :"+e);
            }

            return response.toString();

    }




    @Override
    public JSONObject listSshKeys(HttpServletRequest request,String resourceGroups) throws Exception {

        String inputLine=null;
        StringBuffer response = new StringBuffer();
        JSONObject responseDetailsJson = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        logger.info("getting all ssh keys...");

        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));

        URL url=new URL("https://management.azure.com" +
                "/subscriptions/"+credentials.getSubscriptionId() +
                "/resourceGroups/" +resourceGroups+
                "/providers/Microsoft.Compute/sshPublicKeys?api-version=2021-11-01");

        GetToken getToken = new GetToken(vaultService,applicationProperties, userService);
        logger.debug("url : "+url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization","bearer "+getToken.gettingToken(request.getHeader("userName")));

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in1 = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            while ((inputLine = in1.readLine()) != null) {
                response.append(inputLine);
            }
            logger.debug(response);
           try {
                Object obj = JSONValue.parse(response.toString());
                JSONObject json = (JSONObject) obj;
                JSONArray ja = (JSONArray) json.get("value");
                Iterator itr2 = ja.iterator();
                while (itr2.hasNext()) {
                   Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();

                   while (itr1.hasNext()) {
                       Map.Entry pair = (Map.Entry) itr1.next();

                      if(pair.getKey().equals("name")){
                          JSONObject formDetailsJson = new JSONObject();
                          logger.debug(pair.getKey() + " : " + pair.getValue());
                          formDetailsJson.put("name", pair.getValue());
                          jsonArray.add(formDetailsJson);
                       }

                   }


               }

               responseDetailsJson.put("value", jsonArray);
               logger.info(responseDetailsJson);
            }catch (Exception e) {
               logger.error("Error occurred while listSshKeys:::::"+e.getMessage());
                logger.error("Exception"+e.getMessage());
            }
            in1.close();
        } else {
            logger.debug("GET request not worked");
            throw new Exception("ssh key is not listed try again");
        }
        conn.disconnect();
        logger.debug(responseDetailsJson);
        logger.info("ssh key list ended...");

        return responseDetailsJson ;


    }

    private String generateKeyPair(HttpServletRequest request,String subscriptions,String resourceGroups,String sshPublicKeys,String location) throws IOException, JSONException {

        String output=null;
        String inputLine=null;
        StringBuffer response = new StringBuffer();
        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        URL url=new URL("https://management.azure.com" +
                "/subscriptions/"+subscriptions +
                "/resourceGroups/"+resourceGroups+
                "/providers/Microsoft.Compute" +
                "/sshPublicKeys/"+sshPublicKeys+"/generateKeyPair?api-version=2021-11-01");

        GetToken getToken = new GetToken(vaultService,applicationProperties, userService);
        logger.debug("url : "+url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        conn.setRequestProperty("Authorization","bearer "+getToken.gettingToken(request.getHeader("userName")));
        String data = "{\"location\": \""+location+"\"}";
        logger.debug(data);

        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = conn.getOutputStream();
        stream.write(out);


        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in1 = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));

            while ((inputLine = in1.readLine()) != null) {

                response.append(inputLine);
            }
            in1.close();
        } else {
            logger.info("In Key pair POST request not worked");
        }

        conn.disconnect();

        return response.toString();

    }

    private List<String> gettingSshKeys(HttpServletRequest request,String resourceGroupName) throws Exception {
        List<String> list=new ArrayList<>();
        org.json.JSONObject jsnobject = new org.json.JSONObject(listSshKeys(request,resourceGroupName));
        org.json.JSONArray jsonArray = jsnobject.getJSONArray("value");
        for(int i=0;i<jsonArray.length();i++){

            String name=jsonArray.get(i).toString();
            Object object = JSONValue.parse(name.toString());
            JSONObject jsonObject = (JSONObject) object;
            String sshName=(String) jsonObject.get("name");

            list.add(sshName);
        }
        return  list;
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
