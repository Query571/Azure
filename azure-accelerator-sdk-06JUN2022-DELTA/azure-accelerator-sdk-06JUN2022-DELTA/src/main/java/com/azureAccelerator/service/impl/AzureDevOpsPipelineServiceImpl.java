/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.service.impl;

import com.azureAccelerator.repository.RoleRepository;
import com.azureAccelerator.repository.UserRepository;
import com.azureAccelerator.service.AzureDevOpsPipelineService;
import io.micrometer.core.instrument.util.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.InputStream;
import java.util.stream.Collectors;

@Service
public class AzureDevOpsPipelineServiceImpl implements AzureDevOpsPipelineService {

 private static final Logger logger = LogManager.getLogger(ARMDefaultTemplateServiceImpl.class);

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final BCryptPasswordEncoder encoder;
  private final ModelMapper modelMapper;
  private final String restApiVersion="5.1";

  @Autowired
  public AzureDevOpsPipelineServiceImpl(
          UserRepository userRepository,
          RoleRepository roleRepository,
          BCryptPasswordEncoder encoder,
          ModelMapper modelMapper) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.encoder = encoder;
    this.modelMapper = modelMapper;
  }

  public HashMap<String,String> getPublicAlias(String token) throws IOException {
    URL url=new URL("https://app.vssps.visualstudio.com/_apis/profile/profiles/me?api-version="+restApiVersion+"");
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();
    HashMap<String,String> resp=new HashMap<>();
    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);

    JSONObject responseDetailsJson = new JSONObject();


    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    InputStream is = conn.getErrorStream();

    if(conn.getResponseCode()==401){
      responseDetailsJson.put("Status",new JSONObject().put("Status",String.valueOf(IOUtils.toString(conn.getErrorStream(), StandardCharsets.UTF_8))));
    }

    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String,String> map=new HashMap<String, String>();
    String res=null;
    try {
      Object obj = JSONValue.parse(response.toString());
      JSONObject json = (JSONObject) obj;
      resp.put("displayName",String.valueOf(json.get("displayName")));
      resp.put("publicAlias",String.valueOf(json.get("publicAlias")));
      resp.put("emailAddress",String.valueOf(json.get("emailAddress")));
      resp.put("coreRevision",String.valueOf(json.get("coreRevision")));
      resp.put("timeStamp",String.valueOf(json.get("timeStamp")));
      resp.put("id",String.valueOf(json.get("id")));
      resp.put("revision",String.valueOf(json.get("revision")));
      res= String.valueOf(json.get("publicAlias"));

    }catch (Exception e) {
      logger.error("Error occured while getPublicAlias:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return resp;
  }

  @Override
  public HashMap<String,String> devopsTokenCheck(HttpServletRequest request, String token) throws IOException{

    HashMap<String,String> map=new HashMap<String, String>();
    map.put("displayName",getPublicAlias(decodeString(token)).get("displayName"));
    return map;
  }
  @Override
  public JSONObject getAllOrganization(HttpServletRequest request, String token) throws IOException {
    token=decodeString(token);
    String memberId=getPublicAlias(token).get("publicAlias");
    URL url=new URL("https://app.vssps.visualstudio.com/_apis/accounts?api-version="+restApiVersion+"&memberId="+memberId+"");
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      Object obj = JSONValue.parse(response.toString());
      JSONObject json = (JSONObject) obj;
      responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
        JSONObject formDetailsJson = new JSONObject();
        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }
      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while getAllOrganization:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return responseDetailsJson;
  }

  @Override
  public Map<String,String> storeSecret(HttpServletRequest request,String token,String username) {
    token=decodeString(token);
    Map<String,String> resp=new HashMap<>();
    userRepository.storeSecret(username,token);
    resp.put("200","Success");
    resp.put("token",userRepository.getDevopsToken(username));
    return resp;
  }

  public Map<String,String> getDevopsToken(HttpServletRequest request,String username) {
    Map<String,String> resp=new HashMap<>();
    String token=userRepository.getDevopsToken(username);
    resp.put("200","Success");
    resp.put("token",token);
    return resp;
  }

  @Override
  public JSONObject getProjectList(HttpServletRequest request,String token,String  organization) throws IOException {
    token=decodeString(token);
    organization=organization.replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://dev.azure.com/"+organization+"/_apis/projects?api-version="+restApiVersion+"");
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      Object obj = JSONValue.parse(response.toString());
      JSONObject json = (JSONObject) obj;
      responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();

        JSONObject formDetailsJson = new JSONObject();

        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }

      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while checkSpecificOrgToken:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }


    return responseDetailsJson;

  }

  @Override
  public HashMap<String,String> checkSpecificOrgToken(HttpServletRequest request,String token,String  organization) throws IOException {
    token=decodeString(token);
    organization=organization.replaceAll(" ", "%20").toLowerCase();

    HashMap<String,String> res=new HashMap<>();

    URL url=new URL("https://dev.azure.com/"+organization+"/_apis/projects?api-version="+restApiVersion+"");
    System.out.println("url : "+url.toString());
    String username=null;
    String password=token;
    String userpass = username + ":" + password;
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Content-Type", "application/json");
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    conn.setRequestProperty ("Authorization", basicAuth);
    res.put("code",String.valueOf(conn.getResponseCode()));
    res.put("message",conn.getResponseMessage());

    return res;

  }

  @Override
  public JSONObject getAllPipeline(HttpServletRequest request,String token, String organization, String projectName) throws IOException {
    token=decodeString(token);
    projectName  = projectName.replaceAll(" ", "%20").toLowerCase();
    organization= organization.replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://dev.azure.com/"+organization+"/"+projectName+"/_apis/build/definitions?api-version="+restApiVersion+"");

    System.out.println("url : "+url.toString());
    String username=null;
    String password=token;
    String userpass = username + ":" + password;

    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Content-Type", "application/json");
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    conn.setRequestProperty ("Authorization", basicAuth);

    String inputLine=null;
    StringBuffer response = new StringBuffer();
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            conn.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }

    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();

    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();

    try {
      Object obj = JSONValue.parse(response.toString());

      JSONObject json = (JSONObject) obj;
      responseDetailsJson.put("count", String.valueOf(json.get("count")));

      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {

        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
        JSONObject formDetailsJson = new JSONObject();
        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }

      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return responseDetailsJson;
  }



  @Override
  public JSONObject getAllBuildLogsList(HttpServletRequest request,String token,String  organization,String projectName) throws IOException {
    token=decodeString(token);
    projectName  = projectName.replaceAll(" ", "%20").toLowerCase();
    organization= organization.replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://dev.azure.com/"+organization+"/"+projectName+"/_apis/build/builds?api-version="+restApiVersion+"");
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      Object obj = JSONValue.parse(response.toString());
      JSONObject json = (JSONObject) obj;
      responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();

        JSONObject formDetailsJson = new JSONObject();

        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }

      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }


    return responseDetailsJson;

  }



  @Override
  public JSONObject getAllReleaseDefinition(HttpServletRequest request,String token,String  organization,String projectName) throws IOException {
    token=decodeString(token);
    projectName  = projectName.replaceAll(" ", "%20").toLowerCase();
    organization= organization.replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://vsrm.dev.azure.com/"+organization+"/"+projectName+"/_apis/release/definitions?api-version="+restApiVersion+"");

    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      Object obj = JSONValue.parse(response.toString());
      JSONObject json = (JSONObject) obj;
      responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();

        JSONObject formDetailsJson = new JSONObject();

        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }

      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }


    return responseDetailsJson;

  }

  @Override
  public JSONObject getAllReleaseLogsList(HttpServletRequest request,String token,String  organization,String projectName) throws IOException {
    token=decodeString(token);
    projectName  = projectName.replaceAll(" ", "%20").toLowerCase();
    organization= organization.replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://vsrm.dev.azure.com/"+organization+"/"+projectName+"/_apis/release/releases?api-version="+restApiVersion+"");

    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      Object obj = JSONValue.parse(response.toString());
      JSONObject json = (JSONObject) obj;
      responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();

        JSONObject formDetailsJson = new JSONObject();

        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }

      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }


    return responseDetailsJson;

  }




  @Override
  public JSONObject getAllReleaseByPipeline(HttpServletRequest request,String token,String  organization,String projectName,String definitionId) throws IOException {
    token=decodeString(token);
    projectName  = projectName.replaceAll(" ", "%20").toLowerCase();
    organization= organization.replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://vsrm.dev.azure.com/"+organization+"/"+projectName+"/_apis/release/releases?definitionId="+definitionId+"&api-version="+restApiVersion+"");

    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      Object obj = JSONValue.parse(response.toString());
      JSONObject json = (JSONObject) obj;
      responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();

        JSONObject formDetailsJson = new JSONObject();

        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }

      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }


    return responseDetailsJson;

  }

  @Override
  public JSONObject getAllReleaseTasklog(HttpServletRequest request,String token,String  organization,String projectName,String releaseId,String environmentID,String deployPhasesId) throws IOException {
    token=decodeString(token);
    projectName  = projectName.replaceAll(" ", "%20").toLowerCase();
    organization= organization.replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://vsrm.dev.azure.com/"+organization+"/"+projectName+"/_apis/" +
            "Release/releases/"+releaseId+"/environments/"+environmentID+"/deployPhases/"+deployPhasesId+"/tasks");

    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      Object obj = JSONValue.parse(response.toString());
      JSONObject json = (JSONObject) obj;
      responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();

        JSONObject formDetailsJson = new JSONObject();

        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }

      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }


    return responseDetailsJson;

  }


  @Override
  public JSONObject getReleaseLogsById(HttpServletRequest request,String token,String organization,String projectName,String releaseId) throws IOException {
    token=decodeString(token);
    projectName  = projectName.replaceAll(" ", "%20").toLowerCase();
    organization= organization.replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://vsrm.dev.azure.com/"+organization+"/"+projectName+"/_apis/release/releases/"+releaseId+"?api-version="+restApiVersion+"");
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();
    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));


    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      String newResponse="{ \"value\" : ["+response+"]}";

      Object obj = JSONValue.parse(newResponse.toString());
      JSONObject json = (JSONObject) obj;
      //responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
        JSONObject formDetailsJson = new JSONObject();
        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }
      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return responseDetailsJson;
  }

  @Override
  public JSONObject getAllLogsOfBuild(HttpServletRequest request,String token,String  organization,String projectName,String buildId) throws IOException {
    token=decodeString(token);
    projectName  = projectName.replaceAll(" ", "%20").toLowerCase();
    organization= organization.replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://dev.azure.com/"+organization+"/"+projectName+"/_apis/build/builds/"+buildId+"/logs?api-version="+restApiVersion+"");
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      Object obj = JSONValue.parse(response.toString());
      JSONObject json = (JSONObject) obj;
      responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();

        JSONObject formDetailsJson = new JSONObject();

        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }

      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }


    return responseDetailsJson;

  }


  @Override
  public HashMap<String,StringBuffer> getLogsById(HttpServletRequest request,String token,
                                            String  organization,String projectName,String buildId,String logId) throws IOException {
    token=decodeString(token);
    projectName  = projectName.replaceAll(" ", "%20").toLowerCase();
    organization= organization.replaceAll(" ", "%20").toLowerCase();
    HashMap<String,StringBuffer> hs=new HashMap<>();
    URL url=new URL("https://dev.azure.com/"+organization+"/"+projectName+"/_apis/build/builds/"+buildId+"/logs/"+logId+"?api-version="+restApiVersion+"");
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
      String newLineChar = System.getProperty("line.separator");

      while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
          response.append(newLineChar);

      }
    try {
      hs.put("response",response);
      System.out.println("in response------>"+response);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return hs;

  }

  @Override
  public HashMap<String,StringBuffer> getReleaseLogById(HttpServletRequest request,String token,
                                                  String  organization,String projectName,
                                                  String releaseId,String environmentID,String deployPhasesId,String taskId) throws IOException {
    token=decodeString(token);
    projectName  = projectName.replaceAll(" ", "%20").toLowerCase();
    organization= organization.replaceAll(" ", "%20").toLowerCase();
    HashMap<String,StringBuffer> hs=new HashMap<>();
    URL url=new URL("https://vsrm.dev.azure.com/"+organization+"/"+projectName+"/_apis/Release/" +
            "releases/"+releaseId+"/environments/"+environmentID+"/deployPhases/"+deployPhasesId+"/" +
            "tasks/"+taskId+"/logs?api-version="+restApiVersion+"");
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    String newLineChar = System.getProperty("line.separator");

    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
      response.append(newLineChar);

    }
    try {
      hs.put("response",response);
      System.out.println("in response------>"+response.toString());
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return hs;

  }



  @Override
  public JSONObject getAllLogsList(HttpServletRequest request,String token,String organization,String projectName,String pipelineId,String  runId) throws IOException {
    token=decodeString(token);
    projectName  = projectName.replaceAll(" ", "%20").toLowerCase();
    organization= organization.replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://dev.azure.com/"+organization+"/"+projectName+"/_apis" +
            "/pipelines/"+pipelineId+"/runs?api-version="+restApiVersion+"");
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      Object obj = JSONValue.parse(response.toString());
      JSONObject json = (JSONObject) obj;
      responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
        JSONObject formDetailsJson = new JSONObject();
        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }
      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return responseDetailsJson;
  }

  @Override
  public JSONObject releaseDefinitionsList(HttpServletRequest request,String token,String organization,String projectName) throws IOException {
    token=decodeString(token);
    projectName  = projectName.replaceAll(" ", "%20").toLowerCase();
    organization= organization.replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://vsrm.dev.azure.com/"+organization+"/"+projectName+"/_apis/release/definitions/1?api-version="+restApiVersion+"");
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();
    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));


    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      String newResponse="{ \"value\" : ["+response+"]}";

      Object obj = JSONValue.parse(newResponse.toString());
      JSONObject json = (JSONObject) obj;
      //responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
        JSONObject formDetailsJson = new JSONObject();
        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }
      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return responseDetailsJson;
  }

  @Override
  public JSONObject releaseList(HttpServletRequest request,String token,String organization,String projectName) throws IOException {
    token=decodeString(token);
    projectName  = projectName.replaceAll(" ", "%20").toLowerCase();
    organization= organization.replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://vsrm.dev.azure.com/"+organization+"/"+projectName+"/_apis/release/releases?api-version="+restApiVersion+"");
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      Object obj = JSONValue.parse(response.toString());
      JSONObject json = (JSONObject) obj;
      responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
        JSONObject formDetailsJson = new JSONObject();
        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }
      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return responseDetailsJson;
  }
  @Override
  public JSONObject getStageOfRelease(HttpServletRequest request,String token,String organization,String projectName,String  releaseId) throws IOException {
    token=decodeString(token);
    projectName  = projectName.replaceAll(" ", "%20").toLowerCase();
    organization= organization.replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://vsrm.dev.azure.com/"+organization+"/"+projectName+"/_apis/release/releases/"+releaseId+"?api-version="+restApiVersion+"");
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      String newResponse="{ \"value\" : ["+response+"]}";

      Object obj = JSONValue.parse(newResponse.toString());
      JSONObject json = (JSONObject) obj;
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
        JSONObject formDetailsJson = new JSONObject();
        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }
      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return responseDetailsJson;
  }

  public String getRepositorieId(HttpServletRequest request,String token,String organization,String projectName) throws IOException {
    token=decodeString(token);
    projectName  = projectName.replaceAll(" ", "%20").toLowerCase();
    organization= organization.replaceAll(" ", "%20").toLowerCase();


    URL url=new URL("https://dev.azure.com/"+organization+"/"+projectName+"/_apis/git/repositories?api-version="+restApiVersion+"");
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      Object obj = JSONValue.parse(response.toString());
      JSONObject json = (JSONObject) obj;
      responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
        JSONObject formDetailsJson = new JSONObject();
        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          if (pair.getKey().equals("id")){
            return (String) pair.getValue();
          }

          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }
      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return null;

  }

  public String getDefinitionsById(HttpServletRequest request,String token,String organization,String projectName,
                                   String definitionId,String oldName,String newName,String oldPath,String newPath) throws IOException {
    //token=decodeString(token);
    projectName = projectName .replaceAll(" ", "%20").toLowerCase();
    organization =organization .replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://dev.azure.com/"+organization+"/"+projectName+"/_apis/build/definitions/"+definitionId+"?api-version="+restApiVersion+"");
    System.out.println("url : "+url.toString());
    String username=null;
    String password=token;
    String userpass = username + ":" + password;
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Content-Type", "application/json");
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    conn.setRequestProperty ("Authorization", basicAuth);
    String inputLine=null;
    StringBuffer response = new StringBuffer();
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            conn.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();

    String res=response.toString();
    if(oldName!=null && newName!=null){
      res=res.replace(oldName,newName);
    }

//      if(newPath!=null && oldPath.length()==0){
//        res=res.replace("\\"+"\\",newPath);
//      }else if(newPath.length()==0 && oldPath!=null){
//        res=res.replace(oldPath,"");
//      }else if(oldPath!=null && newPath!=null){
//        res=res.replace(oldPath,newPath);
//      }

    return res;
  }

  @Override
  public JSONObject renamePipeline(HttpServletRequest request,String token,String organization,String projectName,String definitionId,
                                   String oldName,String newName,String oldPath,String newPath) throws IOException {
    token=decodeString(token);
    projectName = projectName .replaceAll(" ", "%20").toLowerCase();
    organization =organization .replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://dev.azure.com/"+organization+"/"+projectName+"/_apis/build/definitions/"+definitionId+"?api-version="+restApiVersion+"");
    String username=null;
    String password=token;
    String userpass = username + ":" + password;
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("PUT");
    conn.setRequestProperty("Content-Type", "application/json");
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    conn.setRequestProperty ("Authorization", basicAuth);
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    conn.setDoOutput(true);

    try(OutputStream os = conn.getOutputStream()) {
      byte[] input = getDefinitionsById(request,token,organization,projectName,definitionId,oldName,newName
              ,oldPath,newPath).toString().getBytes("utf-8");
      os.write(input, 0, input.length);
    }

    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            conn.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
//    System.out.println("response --->"+response);

    try {
      String newResponse="{ \"value\" : ["+response+"]}";
      Object obj = JSONValue.parse(newResponse.toString());
      JSONObject json = (JSONObject) obj;
      JSONArray ja = (JSONArray) json.get("value");
      //System.out.println("(JSONArray) --->"+(JSONArray) json.get("value"));
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {

        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
        JSONObject formDetailsJson = new JSONObject();
        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }
      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return responseDetailsJson;
  }

  public HashMap<String,String> deletePipeline(HttpServletRequest request,String token, String organization, String projectName, String definitionId) throws IOException {
    token=decodeString(token);
    projectName = projectName .replaceAll(" ", "%20").toLowerCase();
    organization =organization .replaceAll(" ", "%20").toLowerCase();


    URL url=new URL("https://dev.azure.com/"+organization+"/"+projectName+"/_apis/build/definitions/"+definitionId+"?api-version="+restApiVersion+"");
    System.out.println("url : "+url.toString());
    String username=null;
    String password=token;
    String userpass = username + ":" + password;
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("DELETE");
    conn.setRequestProperty("Content-Type", "application/json");
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    conn.setRequestProperty ("Authorization", basicAuth);
    String inputLine=null;
    StringBuffer response = new StringBuffer();
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            conn.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    System.out.println("response --->"+response);

    HashMap<String ,String > res=new HashMap<>();
    res.put("code","200");
    res.put("Status","Deleted Sucessfully");
    return res;
  }

  @Override
  public JSONObject repositoriesList(HttpServletRequest request,String token,String organization,String projectName) throws IOException {
    String repositorieId=getRepositorieId(request,token,organization,projectName);

    token=decodeString(token);

    projectName = projectName .replaceAll(" ", "%20").toLowerCase();
    organization =organization .replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://dev.azure.com/"+organization+"/"+projectName+"/_apis/git/repositories/"+repositorieId+"/refs?api-version="+restApiVersion+"");

    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      Object obj = JSONValue.parse(response.toString());
      JSONObject json = (JSONObject) obj;
      responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
        JSONObject formDetailsJson = new JSONObject();
        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }
      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return responseDetailsJson;
  }
  @Override
  public JSONObject runPipelineStatus(HttpServletRequest request,String token,String organization,String projectName,
                                      String pipelineId,String runId) throws IOException {

    token=decodeString(token);
//    URL url=new URL("https://dev.azure.com/"+organization+"/"+projectName+"/" +
//            "_apis/pipelines/"+pipelineId+"/runs/?"+runId+"api-version="+restApiVersion+"");
    projectName = projectName .replaceAll(" ", "%20").toLowerCase();
    organization =organization .replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://dev.azure.com/"+organization+"/"+projectName+"/_apis" +
            "/pipelines/"+pipelineId+"/runs/"+runId+"?api-version="+restApiVersion+"");

    System.out.println("url----->"+url.toString());
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      String newResponse="{ \"value\" : ["+response+"]}";

      Object obj = JSONValue.parse(newResponse.toString());
      JSONObject json = (JSONObject) obj;
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
        JSONObject formDetailsJson = new JSONObject();
        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }
      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return responseDetailsJson;
  }

  @Override
  public JSONObject deployeStageOfRelease(HttpServletRequest request,String token,String organization,String projectName,
                                          String releaseId,String stageId) throws IOException {
    token=decodeString(token);
    projectName = projectName .replaceAll(" ", "%20").toLowerCase();
    organization =organization .replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://vsrm.dev.azure.com/"+organization+"/"+projectName+"/_apis/" +
            "Release/releases/"+releaseId+"/environments/"+stageId+"?api-version=6.0");

    System.out.println("url----->"+url.toString());
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String message;
    JSONObject jsonBody = new JSONObject();
    jsonBody.put("variables",null);
    jsonBody.put("comment",null);
    jsonBody.put("scheduledDeploymentTime",null);
    jsonBody.put("status","inProgress");

    message = jsonBody.toString();
    System.out.println("jsonBody.toString()----->"+jsonBody.toString());

    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");

    conn.setRequestProperty("Content-Type", "application/json");

    InputStream is = conn.getErrorStream();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    conn.setRequestProperty ("Authorization", basicAuth);
    conn.setDoOutput(true);
    try(OutputStream os = conn.getOutputStream()) {
      byte[] input = jsonBody.toString().getBytes("utf-8");
      os.write(input, 0, input.length);
      os.flush();
    }
    JSONObject responseDetailsJson = new JSONObject();

//    System.out.println("error----->"+ IOUtils.toString(conn.getErrorStream(), StandardCharsets.UTF_8));
    if(conn.getResponseCode()==400){
      responseDetailsJson.put("Status",new JSONObject().put("Status",String.valueOf(IOUtils.toString(conn.getErrorStream(), StandardCharsets.UTF_8))));
    }
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            conn.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONArray jsonArray = new JSONArray();
    try {
      String newResponse="{ \"value\" : ["+response+"]}";
      Object obj = JSONValue.parse(newResponse.toString());
      JSONObject json = (JSONObject) obj;
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
        JSONObject formDetailsJson = new JSONObject();
        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }
      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while deployeStageOfRelease:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return responseDetailsJson;
  }


  @Override
  public ResponseEntity<Resource> releasesGetLogs(HttpServletRequest request, HttpServletResponse response, String token, String organization, String projectName, String releaseId) throws IOException {
    token=decodeString(token);
    projectName = projectName .replaceAll(" ", "%20").toLowerCase();
    organization =organization .replaceAll(" ", "%20").toLowerCase();

//    URL url=new URL("https://vsrm.dev.azure.com/"+organization+"/"+projectName+"/_apis/release/releases/"+releaseId+"/logs?api-version="+restApiVersion+"");
    URL url=new URL("https://vsrm.dev.azure.com/rathody/mutiple-branches-github/_apis/release/releases/1/logs?api-version=6.1");
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    //StringBuffer response = new StringBuffer();
    final  Long MILLS_IN_DAY = 86400000L;
    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);

//    response.setContentType("application/zip");
    // response.setHeader("Content-Disposition", "attachment; filename=data.zip");



//    java.io.InputStream in = uc.getInputStream();

//    FileOutputStream out = new FileOutputStream("download.zip");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

    InputStream is =  uc.getInputStream();; // get your input stream here
    Resource resource = new InputStreamResource(is);

    return new ResponseEntity<>(resource, headers, HttpStatus.OK);

//
//    byte[] buf = new byte[10024];
//    int n = in.read(buf);
//    while (n >= 0) {
//      out.write(buf, 0, n);
//      n = in.read(buf);
//    }
////    response = ok((Object) out);
//
//    response.setContentType("application/zip");
//    response.setHeader("Content-Disposition",
//            "attachment; filename= - " + new Date().toString() + ".zip\"");
//    return ResponseEntity
//            .ok()
//            .header("Content-Disposition", "attachment;filename=export.zip")
//            .header("Content-Type","application/octet-stream")
//            .body(bos.toByteArray());
////    StreamUtils.copy(out, response.getOutputStream());
//
//    out.flush();
//    out.close();
//    in.close();
//    response.flushBuffer();

    // You might also wanna disable caching the response
//    try (ZipInputStream zis = new ZipInputStream(uc.getInputStream())) {
//      ZipEntry ze;
//
//      ZipEntry entry; // kinda self-explained (a file inside zip)
//      byte[] zipContent = zis.readAllBytes();
//System.out.println("zipContent-----<"+zipContent);
//   ///   new FileOutputStream("some.zip").write(zipContent);
//
//      try ( ZipOutputStream zos = new ZipOutputStream(response.getOutputStream()) ) {
////           Add zip entries you want to include in the zip file
//
////            FileSystemResource resource = new FileSystemResource( zis.readAllBytes());
//
//        //          zippedOut.putNextEntry(e);
//        // And the content of the resource:
//        StreamUtils.copy(zis.readAllBytes(), zos);
//        zos.closeEntry();
//
//        zos.finish();
//      }
//      catch (Exception e) {
//        // Exception handling goes here
//      }
//      while ((ze = zis.getNextEntry()) != null) {
//        System.out.println("zipContent-----<"+ze.getName());
//        System.out.println("string-----<"+ze.toString());
//
//
////        System.out.format("File: %s Size: %d last modified July 15, 2022",
////                ze.getName(), ze.getSize(),
////                LocalDate.ofEpochDay(ze.getTime() / MILLS_IN_DAY));
//      }
//    } catch (Exception e) {
//      // Exception handling goes here
//    }


//    response.setContentType("application/zip");
//    response.setHeader("Content-Disposition", "attachment; filename=download.zip");
//    try(ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
//        FileSystemResource fileSystemResource = new FileSystemResource("fileName.zip");
//        ZipEntry zipEntry = new ZipEntry(fileSystemResource.getFilename());
//        zipEntry.setSize(fileSystemResource.contentLength());
//        zipEntry.setTime(System.currentTimeMillis());
//
//        zipOutputStream.putNextEntry(zipEntry);
//
//        StreamUtils.copy(fileSystemResource.getInputStream(), zipOutputStream);
//        zipOutputStream.closeEntry();
//
//      zipOutputStream.finish();
//    } catch (IOException e) {
//      logger.error(e.getMessage(), e);
//    }
//    response.setContentType("application/octet-stream");
//    response.setHeader("Content-Disposition", "attachment;filename=download.zip");
//    response.setStatus(HttpServletResponse.SC_OK);

//        try (ZipInputStream zis = new ZipInputStream(uc.getInputStream())) {
//
//      ZipEntry entry; // kinda self-explained (a file inside zip)
//      byte[] zipContent = zis.readAllBytes();
//
//      new FileOutputStream("some.zip").write(zipContent);
//          try (ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream())) {
//            FileSystemResource resource = new FileSystemResource(zipContent);
//
//  //          zippedOut.putNextEntry(e);
//            // And the content of the resource:
//            StreamUtils.copy(resource.getInputStream(), zippedOut);
//            zippedOut.closeEntry();
//
//            zippedOut.finish();
//        } catch (Exception e) {
//          // Exception handling goes here
//        }

//      while ((entry = zis.getNextEntry()) != null) {
//        // do whatever you need :)
//        // this is just a dummy stuff
//        System.out.format("File: %s Size: %d ",
//                entry.getName(), entry.getSize());
//      }
//    }


//    try (ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream())) {
//        FileSystemResource resource = new FileSystemResource(file);
//
//        zippedOut.putNextEntry(e);
//        // And the content of the resource:
//        StreamUtils.copy(resource.getInputStream(), zippedOut);
//        zippedOut.closeEntry();
//
//      //      for (String file : fileNames) {
////        FileSystemResource resource = new FileSystemResource(file);
////
////        ZipEntry e = new ZipEntry(resource.getFilename());
////        // Configure the zip entry, the properties of the file
////        e.setSize(resource.contentLength());
////        e.setTime(System.currentTimeMillis());
////        // etc.
////        zippedOut.putNextEntry(e);
////        // And the content of the resource:
////        StreamUtils.copy(resource.getInputStream(), zippedOut);
////        zippedOut.closeEntry();
////      }
//      zippedOut.finish();
//    } catch (Exception e) {
//      // Exception handling goes here
//    }


  }

  @Override
  public JSONObject getAllLogByLogId(HttpServletRequest request,String token,String organization,String projectName,String pipelineId,String  runId,String  logId) throws IOException {
    token=decodeString(token);

    projectName = projectName .replaceAll(" ", "%20").toLowerCase();
    organization =organization .replaceAll(" ", "%20").toLowerCase();
    URL url=new URL("https://dev.azure.com/"+organization+"/"+projectName+"/_apis/" +
            "pipelines/"+pipelineId+"/" +
            "runs/"+runId+"/logs/"+logId+"?api-version="+restApiVersion+"");
    URLConnection uc = url.openConnection();
    String username=null;
    String password=token;
    String inputLine=null;
    StringBuffer response = new StringBuffer();

    String userpass = username + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    uc.setRequestProperty ("Authorization", basicAuth);
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            uc.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      Object obj = JSONValue.parse(response.toString());
      JSONObject json = (JSONObject) obj;
      responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {
        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
        JSONObject formDetailsJson = new JSONObject();
        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }
      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return responseDetailsJson;
  }

  public JSONObject runPipeline( HttpServletRequest request,String token,String organization,String projectName,String pipelineId,String branch) throws IOException {
    token=decodeString(token);
    String username=null;
    String password=token;
    String output = null;
    String inputLine = null;
    StringBuffer response = new StringBuffer();
    //String sshKeys = "";
    projectName =projectName.replaceAll(" ", "%20").toLowerCase();
    organization=organization.replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://dev.azure.com/"+organization+"/"+projectName+"/_apis/" +
            "pipelines/"+ pipelineId+"/runs?api-version="+restApiVersion+"");
    String userpass = username + ":" + password;
    String message;
    JSONObject json = new JSONObject();
    json.put("refName", branch);
    JSONObject json1 = new JSONObject();
    json1.put("self", json);
    JSONObject json2 = new JSONObject();
    json2.put("repositories", json1);
    JSONObject json3 = new JSONObject();
    json3.put("resources", json2);
    message = json3.toString();

    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    conn.setRequestProperty ("Authorization", basicAuth);
    conn.setDoOutput(true);
    try(OutputStream os = conn.getOutputStream()) {
      byte[] input = json3.toString().getBytes("utf-8");
      os.write(input, 0, input.length);
    }
    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { //success
      BufferedReader in1 = new BufferedReader(new InputStreamReader(
              conn.getInputStream()));
      while ((inputLine = in1.readLine()) != null) {
        response.append(inputLine);
      }
      in1.close();
    } else{
      System.out.println("GET request not worked");
    }
    conn.disconnect();
    return (JSONObject) JSONValue.parse(response.toString());
  }

  @Override
  public JSONObject runsListForPipeline(HttpServletRequest request,String token,String organization,String projectName,String pipelineId) throws IOException {
    token=decodeString(token);
    projectName = projectName .replaceAll(" ", "%20").toLowerCase();
    organization =organization .replaceAll(" ", "%20").toLowerCase();

    URL url=new URL("https://dev.azure.com/"+organization+"/"+projectName+"/_apis/" +
            "pipelines/"+pipelineId+"/runs?api-version="+restApiVersion+"");
    System.out.println("url : "+url.toString());
    String username=null;
    String password=token;
    String userpass = username + ":" + password;
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Content-Type", "application/json");
    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
    conn.setRequestProperty ("Authorization", basicAuth);
    String inputLine=null;
    StringBuffer response = new StringBuffer();
    BufferedReader in1 = new BufferedReader(new InputStreamReader(
            conn.getInputStream()));
    while ((inputLine = in1.readLine()) != null) {
      response.append(inputLine);
    }
    ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> map=new HashMap<String, Object>();
    JSONObject responseDetailsJson = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    System.out.println("response --->"+response);
    try {
      Object obj = JSONValue.parse(response.toString());
      JSONObject json = (JSONObject) obj;
      responseDetailsJson.put("count", String.valueOf(json.get("count")));
      JSONArray ja = (JSONArray) json.get("value");
      //System.out.println("(JSONArray) --->"+(JSONArray) json.get("value"));
      Iterator itr2 = ja.iterator();
      while (itr2.hasNext()) {

        Iterator itr1 = ((Map) itr2.next()).entrySet().iterator();
        JSONObject formDetailsJson = new JSONObject();
        while (itr1.hasNext()) {
          Map.Entry pair = (Map.Entry) itr1.next();
          formDetailsJson.put(pair.getKey(), pair.getValue());
        }
        jsonArray.add(formDetailsJson);
      }
      responseDetailsJson.put("value", jsonArray);
    }catch (Exception e) {
      logger.error("Error occured while listSshKeys:::::"+e.getMessage());
      System.out.println("Exception"+e.getMessage());
    }
    return responseDetailsJson;
  }


  private String decodeString(String encodedString){
    Base64.Decoder decoder = Base64.getDecoder();
    return new String(decoder.decode(encodedString));
  }


}
