package com.azureAccelerator.util;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
//import org.apache.log4j.Logger;
import com.azureAccelerator.service.impl.ARMDefaultTemplateServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GetToken {
    //private static final Logger logger = Logger.getLogger(GetToken.class);
    private static final Logger logger = LogManager.getLogger(ARMDefaultTemplateServiceImpl.class);
    private final VaultService vaultService;
    private final ApplicationProperties applicationProperties;
    private final UserService userService;

    public GetToken(VaultService vaultService, ApplicationProperties applicationProperties, UserService userService) {
        this.vaultService = vaultService;
        this.applicationProperties = applicationProperties;
        this.userService = userService;
    }

    public String gettingToken(String userName) throws IOException, JSONException {

        List<LocalUserDto> userDtoList=userService.findByName(userName);

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
        String acessToken="";
        String inputLine=null;
        StringBuffer response = new StringBuffer();

        try {

            URL url = new URL("https://login.microsoftonline.com/"+secret.getTenantId()+"/oauth2/token");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "grant_type=client_credentials&client_id="+secret.getClientId()+"&client_secret="+secret.getClientSecret()+"&resource=https://management.azure.com/";

            byte[] out = data.getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);

            logger.debug(http.getResponseCode() + " " + http.getResponseMessage());

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        http.getInputStream()));


                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);

                }
                try {

                    Object obj = JSONValue.parse(response.toString());
                    JSONObject jsonObject = (JSONObject) obj;
                    acessToken = (String) jsonObject.get("access_token");
                }catch (Exception e) {
                    logger.error("Exception"+e);
                }

                in.close();
            } else {
                logger.error("POST request not worked");
            }

            http.disconnect();

        } catch (IOException e) {
            logger.error("Error occurred while getting token:::::"+e.getMessage());
        }
        return acessToken;

    }


    public String gettingVaultToken(String userName) throws IOException, JSONException {

        List<LocalUserDto> userDtoList=userService.findByName(userName);

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
        String acessToken="";
        String inputLine=null;
        StringBuffer response = new StringBuffer();
        //JSONObject obj=new JSONObject();

        try {

            URL url = new URL("https://login.microsoftonline.com/"+secret.getTenantId()+"/oauth2/token");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "grant_type=client_credentials&client_id="+secret.getClientId()+"&client_secret="+secret.getClientSecret()+"&resource=https://vault.azure.net";

            byte[] out = data.getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);

            logger.info(http.getResponseCode() + " " + http.getResponseMessage());

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        http.getInputStream()));


                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);

                }
                try {

                    Object obj = JSONValue.parse(response.toString());
                    JSONObject jsonObject = (JSONObject) obj;
                    acessToken = (String) jsonObject.get("access_token");
                }catch (Exception e) {
                    logger.error("Exception"+e);
                }

                in.close();
            } else {
                logger.error("POST request not worked");
            }

            http.disconnect();


        } catch (IOException e) {
            logger.error("Error occurred while value token:::::"+e.getMessage());
        }
        return acessToken;

    }


}
