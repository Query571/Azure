package com.azureAccelerator.service.impl;

import com.azureAccelerator.dto.AzureCredentials;
import com.azureAccelerator.service.DockerHubService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONValue;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class DockerHubServiceImpl implements DockerHubService {

    private static final Logger logger = LogManager.getLogger(DockerHubServiceImpl.class);

    @Override
    public Object loginDockerHub(String userName, String password) throws Exception {


        String inputLine=null;
        StringBuffer response = new StringBuffer();
        logger.info("login DockerHub...");

        //AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));

        URL url=new URL("https://hub.docker.com/v2/users/login");

        //GetToken getToken = new GetToken(vaultService,applicationProperties, userService);
        logger.debug("url : " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        //conn.setRequestProperty("location",sshKeyDto.getLocation());
        //conn.setRequestProperty("Authorization","bearer "+getToken.gettingToken(request.getHeader("userName")));

        String data = "{\n" +
                "    \"username\": \""+userName+"\",\n" +
                "    \"password\": \""+password+"\"\n" +
                "}";
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
        }else{
            throw new Exception("Bad Credentials. UserName or Password is wrong. please check.");
        }
        conn.disconnect();
        return JSONValue.parse(response.toString());
    }

    @Override
    public Object getRepositories(String userNameSpace,String token) throws Exception {
        String inputLine = null;
        StringBuffer response = new StringBuffer();


        URL url = new URL("https://hub.docker.com/v2/namespaces/"+userNameSpace+"/repositories");

        logger.info("url : " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "bearer " + token);
        System.out.println("nnn"+conn.getResponseCode());

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in1 = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));


            while ((inputLine = in1.readLine()) != null) {
                response.append(inputLine);

            }
            logger.debug("response :" + response.toString());
            in1.close();
        } else{
            throw new Exception("something wrong in your docker hub. pls check in your docker account ");
        }

        conn.disconnect();

        return JSONValue.parse(response.toString());


    }

    @Override
    public Object getRepositoriesImagesAndTags(String userNameSpace, String repositories, String token) throws Exception {
        String inputLine = null;
        StringBuffer response = new StringBuffer();


        URL url = new URL("https://hub.docker.com/v2/namespaces/"+userNameSpace+"/repositories/"+repositories+"/images?page=1&page_size=200000");

        logger.info("url : " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "bearer " + token);


        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in1 = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));


            while ((inputLine = in1.readLine()) != null) {
                response.append(inputLine);

            }
            logger.debug("response :" + response.toString());
            in1.close();
        } else{
            throw new Exception("something wrong in your docker hub. pls check in your docker account ");
        }

        conn.disconnect();

        return JSONValue.parse(response.toString());
    }

}
