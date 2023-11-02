package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.*;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.AKSDeployService;
import com.azureAccelerator.service.NetworkSecurityGroupsService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.azureAccelerator.util.GetToken;
import com.google.gson.Gson;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.http.HttpStatus;
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
public class NetworkSecurityGroupsServiceImpl implements NetworkSecurityGroupsService {

    private static final Logger logger = LogManager.getLogger(NetworkSecurityGroupsServiceImpl.class);

    private final VaultService vaultService;
    private final ApplicationProperties applicationProperties;
    private AKSDeployService aksDeployService;
    private final UserService userService;


    public NetworkSecurityGroupsServiceImpl(VaultService vaultService, ApplicationProperties applicationProperties, AKSDeployService aksDeployService, UserService userService) {
        this.vaultService = vaultService;
        this.applicationProperties = applicationProperties;
        this.aksDeployService = aksDeployService;
        this.userService = userService;
    }


   @Override
    public String createNSG(HttpServletRequest request, NetworkSecurityGroupsDto networkSecurityGroupsDto) throws Exception {
        String output=null;
        String inputLine=null;
        StringBuffer response = new StringBuffer();


            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            logger.info("creating nsg...");

            URL url = new URL("https://management.azure.com/" +
                    "subscriptions/" + credentials.getSubscriptionId() + "/" +
                    "resourceGroups/" + networkSecurityGroupsDto.getResourceGroupName() + "/providers/Microsoft.Network/" +
                    "networkSecurityGroups/" + networkSecurityGroupsDto.getName() + "?api-version=2021-05-01");

            GetToken getToken = new GetToken(vaultService, applicationProperties, userService);
            logger.info("url : " + url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            conn.setRequestProperty("Authorization", "bearer " + getToken.gettingToken(request.getHeader("userName")));


            Map<String, String> map = networkSecurityGroupsDto.getTags();
            if (networkSecurityGroupsDto.getTags() == null) {
                map = new HashMap<>();
                networkSecurityGroupsDto.setTags(map);
            }
            map.put("nsgType", "private");
            map.put("resourceGroupName",networkSecurityGroupsDto.getResourceGroupName());

            List<SecurityRulesDto> securityRules = networkSecurityGroupsDto.getProperties().getSecurityRules();
            for (SecurityRulesDto s : securityRules) {

                PropertiesDto propertiesDto = s.getProperties();
                if ((propertiesDto.getDirection().equals("Inbound")) &&
                        (propertiesDto.getSourceAddressPrefix().equals("*")) ||
                        (propertiesDto.getSourceAddressPrefix().equals("0.0.0.0/0"))) {

                    map.put("nsgType", "public");

                }
            }

            logger.debug("NSG Region : "+ networkSecurityGroupsDto.getLocation());
            String properties = "{\n" +
                    "  \"properties\":" + new Gson().toJson(networkSecurityGroupsDto.getProperties()) + " ,\n" +
                    "  \"location\": \"" + networkSecurityGroupsDto.getLocation() + "\",\n" +
                    "  \"tags\": " + new Gson().toJson(networkSecurityGroupsDto.getTags()) + "\n" +
                    "}";


            byte[] properties1 = properties.getBytes(StandardCharsets.UTF_8);

            OutputStream stream = conn.getOutputStream();

            stream.write(properties1);

            logger.debug(conn.getResponseCode() + "-----" + conn.getResponseMessage());

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK || conn.getResponseCode() == HttpURLConnection.HTTP_CREATED ||
                    conn.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED) { //success


                BufferedReader in1 = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));

                while ((inputLine = in1.readLine()) != null) {
                    response.append(inputLine);
                }
                logger.debug(response);
                in1.close();
            }else{
                logger.error("You are given wrong details of NSG or NSG rules");
                throw new Exception("You are given wrong details of NSG or NSG rules");
            }


       String s=getNSG(request,networkSecurityGroupsDto.getResourceGroupName(),networkSecurityGroupsDto.getName());
            logger.debug("NSG details :"+s);
        logger.info("nsg created...");
        return s;
    }

    @Override
    public JSONObject listAllNSG(HttpServletRequest request,String resourceGroupName) throws IOException, JSONException {
        String output = null;
        String inputLine = null;
        StringBuffer response = new StringBuffer();
        org.json.simple.JSONObject json = null;
        logger.info("getting all NSGs...");
        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        URL url = null;
        if(resourceGroupName != null && resourceGroupName.length() > 0)
        {
            url = new URL("https://management.azure.com/" +
                    "subscriptions/"+credentials.getSubscriptionId()+"/" +
                    "resourceGroups/"+resourceGroupName+"/providers/Microsoft.Network/networkSecurityGroups?api-version=2021-05-01");

        }else {

            url = new URL("https://management.azure.com/" +
                    "subscriptions/" + credentials.getSubscriptionId() + "/providers/Microsoft.Network/" +
                    "networkSecurityGroups?api-version=2021-05-01");
        }

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
            logger.debug("Nsg list :"+response);
            Object obj = JSONValue.parse(response.toString());
            json = (org.json.simple.JSONObject) obj;

            in1.close();
        } else{
            logger.debug("GET request not worked");
        }

        conn.disconnect();

        return json;

    }

    @Override
    public String getNSG(HttpServletRequest request,String resourceGroupName, String networkSecurityGroupName) throws IOException, JSONException {
        String output = null;
        String inputLine = null;
        StringBuffer response = new StringBuffer();


        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        URL url = new URL("https://management.azure.com/" +
                "subscriptions/"+credentials.getSubscriptionId()+"/" +
                "resourceGroups/"+resourceGroupName+"/providers/Microsoft.Network/" +
                "networkSecurityGroups/"+networkSecurityGroupName+"?api-version=2021-05-01");

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

            in1.close();
        } else{
            logger.debug("GET request not worked");
        }

        conn.disconnect();

        return response.toString();
    }

    @Override
    public JSONObject getNSG1(HttpServletRequest request,String resourceGroupName, String networkSecurityGroupName) throws IOException, JSONException {
        String output = null;
        String inputLine = null;
        StringBuffer response = new StringBuffer();
        JSONObject json=null;

        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        URL url = new URL("https://management.azure.com/" +
                "subscriptions/"+credentials.getSubscriptionId()+"/" +
                "resourceGroups/"+resourceGroupName+"/providers/Microsoft.Network/" +
                "networkSecurityGroups/"+networkSecurityGroupName+"?api-version=2021-05-01");

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
            Object obj = JSONValue.parse(response.toString());
            json = (org.json.simple.JSONObject) obj;
            in1.close();
        } else{
            logger.debug("GET request not worked");
        }

        conn.disconnect();

        return json;
    }

    @Override
    public Map<String,String> deleteNSG(HttpServletRequest request,String resourceGroupName, String networkSecurityGroupName) throws IOException, JSONException {

        Map<String,String> map=new HashMap<>();

        try {

        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        Azure azure = Azure.configure() // Initial an Azure.Configurable object
                .authenticate(credentials.getApplicationTokenCredentials())
                .withSubscription(credentials.getSubscriptionId());

        String ng="/subscriptions/"+credentials.getSubscriptionId()+"/resourceGroups/"+resourceGroupName+"/providers/Microsoft.Network/networkSecurityGroups/"+networkSecurityGroupName;

            logger.info("Nsg is deleting...");
            logger.debug("nsg id :"+ng);
            azure.networkSecurityGroups().deleteById(ng);
            map.put("status", networkSecurityGroupName + " deleted");
            logger.info("Nsg is deleted...");
        } catch (
                CloudException e) {
            logger.error("delete NSG Exception : "+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }


        return map;
    }

  @Override
    public Map<String,String> updateSubnet(HttpServletRequest request,String resourceGroupName, String vNetName,String subnetName,String nsg) throws JSONException {

        String nsg_id_str = null;
        Network network = null;
        Map<String,String> map = new HashMap<>();
        logger.info("Nsg is association is starting...");
        try {
        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        Azure azure = Azure.configure() // Initial an Azure.Configurable object
                .authenticate(credentials.getApplicationTokenCredentials())
                .withSubscription(credentials.getSubscriptionId());

        nsg_id_str="/subscriptions/"+credentials.getSubscriptionId()+"/resourceGroups/"+resourceGroupName+"/providers/Microsoft.Network/" +
                "networkSecurityGroups/"+nsg;
        logger.debug("NSG id :"+nsg_id_str);
        String net_id_str="/subscriptions/"+credentials.getSubscriptionId()+"/" +
                "resourceGroups/"+resourceGroupName+"/providers/Microsoft.Network/virtualNetworks/"+vNetName;
        logger.debug("Vnet id :"+net_id_str);


        logger.info("sourceNetwork>>>"+nsg_id_str);
         network=azure.networks().getById(net_id_str)
                         .update().updateSubnet(subnetName)
                         .withExistingNetworkSecurityGroup(nsg_id_str).parent().apply();
         logger.debug("Network :"+network.name());
         map.put("status", "Associated Successfully");
         logger.info(map.toString());

     } catch (
     CloudException e) {
            logger.error("AssociationSubnet Exception ; "+e.getMessage());
          throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                HttpStatus.INTERNAL_SERVER_ERROR);
     }

        return map;
    }

    @Override
    public Map<String,String> disAssociationSubnet(HttpServletRequest request,String resourceGroupName, String vNetName, String subnetName) throws JSONException {
        String nsg_id_str = null;
        Network network = null;
        Map<String,String> map=new HashMap<>();

        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            logger.info("Nsg is dis-Association is starting...");

            String net_id_str="/subscriptions/"+credentials.getSubscriptionId()+"/" +
                    "resourceGroups/"+resourceGroupName+"/providers/Microsoft.Network/virtualNetworks/"+vNetName;
            logger.debug("Vnet id :"+net_id_str);

            network=azure.networks().getById(net_id_str)
                    .update().updateSubnet(subnetName)
                    .withoutNetworkSecurityGroup().parent().apply();
            logger.debug(network.name());
                map.put("status", "DisAssociated Successfully");
                logger.info(map.toString());



        } catch (
                CloudException e) {
            logger.error("disAssociationSubnet Exception ; "+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }


        return map;
    }

    @Override
    public String getSubNet(HttpServletRequest request,String resourceGroupName, String vNetName, String subnetName) throws IOException, JSONException {
        String output = null;
        String inputLine = null;
        StringBuffer response = new StringBuffer();

        org.json.simple.JSONObject json = null;
        org.json.simple.JSONObject json3;

        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        URL url = new URL("https://management.azure.com/" +
                "subscriptions/"+credentials.getSubscriptionId() +
                "/resourceGroups/"+resourceGroupName+"/providers/Microsoft.Network/" +
                "virtualNetworks/"+vNetName+"/" +
                "subnets/"+subnetName+"?api-version=2021-05-01");

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

            Object obj = JSONValue.parse(response.toString());
            json = (org.json.simple.JSONObject) obj;
            Object obj1=json.get("properties");

            org.json.simple.JSONObject json2 = (org.json.simple.JSONObject) obj1;
            if(json2.get("networkSecurityGroup")!=null) {
                Object obj2 = json2.get("networkSecurityGroup");
                json3 = (org.json.simple.JSONObject) obj2;

                String str = json3.get("id").toString();
                /*String[] sp*/
                String nsgName = str.substring(str.lastIndexOf("/") + 1);

                json3.put("name", nsgName);


                String nsg = getNSG(request,resourceGroupName, nsgName);
                Object nsgObj = JSONValue.parse(nsg);
                org.json.simple.JSONObject nsgJson = (org.json.simple.JSONObject) nsgObj;


                Object nsgObj2 = nsgJson.get("tags");
                org.json.simple.JSONObject nsgJson3 = (org.json.simple.JSONObject) nsgObj2;

                String str1 = nsgJson3.get("nsgType").toString();
                json.put("networkSecurityGroupType", str1);


                logger.info("type>>>>   " + json.toString());
            }

            in1.close();
        } else{
            logger.debug("GET request not worked");
        }

        conn.disconnect();

        return json.toString();
    }

    @Override
    public NetworkSecurityGroupsResponseDto addNSGRule(HttpServletRequest request,NetworkSecurityGroupsDto networkSecurityGroupsDto) throws Exception {
        NetworkSecurityGroup networkSecurityGroup;
        Map<String,String> map=networkSecurityGroupsDto.getTags();
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));

            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            String id = "/subscriptions/"+credentials.getSubscriptionId()+"/resourceGroups/"+networkSecurityGroupsDto.getResourceGroupName()+"/providers/Microsoft.Network/networkSecurityGroups/"+networkSecurityGroupsDto.getName();

            networkSecurityGroup = azure.networkSecurityGroups().getById(id);
            Collection<NetworkSecurityRule> list=networkSecurityGroup.securityRules().values();
            for(NetworkSecurityRule rule:list){
                if(rule.name().equals(networkSecurityGroupsDto.getRuleName())){
                    throw new Exception("Rule name is already present. Rule name must be unique");
                }
            }

            //networkSecurityGroup.update().withTags(networkSecurityGroupsDto.getTags()).apply();
            if (networkSecurityGroupsDto.getBound().equalsIgnoreCase("Inbound")) {
                if(networkSecurityGroupsDto.getFromAddress().equals("0.0.0.0/0") ||
                        networkSecurityGroupsDto.getSourceRange().equals("*") ){
                    networkSecurityGroup.update().withTag("nsgType", "public").apply();
                }
                if (networkSecurityGroupsDto.isPermission()) {
                    if(networkSecurityGroupsDto.getSourceRange().equals("*") && !networkSecurityGroupsDto.getDestinationRange().equals("*")){
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .allowInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }else if(networkSecurityGroupsDto.getDestinationRange().equals("*") && !networkSecurityGroupsDto.getSourceRange().equals("*")){
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .allowInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }else if(networkSecurityGroupsDto.getDestinationRange().equals("*") &&
                            networkSecurityGroupsDto.getSourceRange().equals("*")){
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .allowInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }else {
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .allowInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }
                } else {
                    if(networkSecurityGroupsDto.getSourceRange().equals("*") && !networkSecurityGroupsDto.getDestinationRange().equals("*")){
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .denyInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }else if(networkSecurityGroupsDto.getDestinationRange().equals("*") && !networkSecurityGroupsDto.getSourceRange().equals("*")){
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .denyInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }else if(networkSecurityGroupsDto.getDestinationRange().equals("*") &&
                            networkSecurityGroupsDto.getSourceRange().equals("*")){
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .denyInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }else {
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .denyInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }
                }

            } else if (networkSecurityGroupsDto.getBound().equalsIgnoreCase("Outbound")) {
                if (networkSecurityGroupsDto.isPermission()) {
                    if(networkSecurityGroupsDto.getSourceRange().equals("*") && !networkSecurityGroupsDto.getDestinationRange().equals("*")){
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .allowOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }else if(networkSecurityGroupsDto.getDestinationRange().equals("*") && !networkSecurityGroupsDto.getSourceRange().equals("*")){
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .allowOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }else if(networkSecurityGroupsDto.getDestinationRange().equals("*") &&
                            networkSecurityGroupsDto.getSourceRange().equals("*")){
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .allowOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }else {
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .allowOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }
                } else {
                    if(networkSecurityGroupsDto.getSourceRange().equals("*") &&  !networkSecurityGroupsDto.getDestinationRange().equals("*")){
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .denyOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }else if(networkSecurityGroupsDto.getDestinationRange().equals("*") &&  !networkSecurityGroupsDto.getSourceRange().equals("*")){
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .denyOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }else if(networkSecurityGroupsDto.getDestinationRange().equals("*") &&
                            networkSecurityGroupsDto.getSourceRange().equals("*")){
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .denyOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }else {
                        networkSecurityGroup.update()
                                .defineRule(networkSecurityGroupsDto.getRuleName())
                                .denyOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .attach().apply();
                    }
                   /* networkSecurityGroup.update()
                            .defineRule(networkSecurityGroupsDto.getRuleName())
                            .denyOutbound()
                            .fromAddress(networkSecurityGroupsDto.getFromAddress())
                            //.fromPortRange(networkSecurityGroupsDto.getSourceFromRange(), networkSecurityGroupsDto.getSourceToRange())
                            .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                            .toAddress(networkSecurityGroupsDto.getToAddress())
                            //.toPortRange(networkSecurityGroupsDto.getDestinationFromRange(),networkSecurityGroupsDto.getDestinationToRange())
                            .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                            .withProtocol(networkSecurityGroupsDto.getProtocol())
                            .withPriority(networkSecurityGroupsDto.getPriority())
                            .attach().apply();*/
                }

            }
        }catch (CloudException e) {
        logger.error("Error occurred :::::"+e.getMessage());
        throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }


        return NetworkSecurityGroupsResponseDto.builder()
                .name(networkSecurityGroup.name())
                .id(networkSecurityGroup.id())
                .resourceGroupName(networkSecurityGroup.resourceGroupName())
                .tags(networkSecurityGroup.tags())
                .build();
    }

    @Override
    public Map<String, String> removeRule(HttpServletRequest request, String resourceGroupName, String myNsg, String ruleName) throws Exception {
        NetworkSecurityGroup networkSecurityGroup;
        Map<String, String> map=new HashMap<>();
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));

            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            String id = "/subscriptions/" + credentials.getSubscriptionId() + "/resourceGroups/" + resourceGroupName + "/providers/Microsoft.Network/networkSecurityGroups/" + myNsg;

            networkSecurityGroup = azure.networkSecurityGroups().getById(id);
            networkSecurityGroup.update().withoutRule(ruleName).apply();
            map.put("status",ruleName+" is successfully removed");
        }catch (CloudException e) {
            logger.error("Error occurred :::::" + e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return map;
    }

    @Override
    public NetworkSecurityGroupsResponseDto updateNsgRule(HttpServletRequest request, NetworkSecurityGroupsDto networkSecurityGroupsDto) throws Exception {
        NetworkSecurityGroup networkSecurityGroup;
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));

            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            String id = "/subscriptions/"+credentials.getSubscriptionId()+"/resourceGroups/"+networkSecurityGroupsDto.getResourceGroupName()+"/providers/Microsoft.Network/networkSecurityGroups/"+networkSecurityGroupsDto.getName();

            networkSecurityGroup = azure.networkSecurityGroups().getById(id);

           /* Collection<NetworkSecurityRule> list=networkSecurityGroup.securityRules().values();
           *//* for(NetworkSecurityRule rule:list){
                if(rule.name().equals(networkSecurityGroupsDto.getRuleName())){
                    throw new Exception("Rule name is not available");
                }
            }*/

            //networkSecurityGroup.update().withTags(networkSecurityGroupsDto.getTags()).apply();
            if (networkSecurityGroupsDto.getBound().equalsIgnoreCase("Inbound")) {
                if(networkSecurityGroupsDto.getFromAddress().equals("0.0.0.0/0") || networkSecurityGroupsDto.getToAddress().equals("0.0.0.0/0")){
                    networkSecurityGroup.update().withTag("nsgType", "public").apply();
                }
                if (networkSecurityGroupsDto.isPermission()) {
                    if (networkSecurityGroupsDto.getSourceRange().equals("*") &&
                            !networkSecurityGroupsDto.getDestinationRange().equals("*")) {
                        networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .allowInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    } else if (networkSecurityGroupsDto.getDestinationRange().equals("*") && !networkSecurityGroupsDto.getSourceRange().equals("*") ) {
                        networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .allowInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    } else if (networkSecurityGroupsDto.getSourceRange().equals("*") &&
                            networkSecurityGroupsDto.getDestinationRange().equals("*")) {
                        networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .allowInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    } else {
                        networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .allowInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                //.fromPortRange(networkSecurityGroupsDto.getSourceFromRange(), networkSecurityGroupsDto.getSourceToRange())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                //.toPortRange(networkSecurityGroupsDto.getDestinationFromRange(),networkSecurityGroupsDto.getDestinationToRange())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    }
                } else {
                    if (networkSecurityGroupsDto.getSourceRange().equals("*") &&
                            !networkSecurityGroupsDto.getDestinationRange().equals("*")) {
                         networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .denyInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    }else if (networkSecurityGroupsDto.getDestinationRange().equals("*") && !networkSecurityGroupsDto.getSourceRange().equals("*") ) {
                        networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .denyInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    }else if (networkSecurityGroupsDto.getSourceRange().equals("*") &&
                            networkSecurityGroupsDto.getDestinationRange().equals("*")) {
                        networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .denyInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    }else {
                        networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .denyInbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                //.fromPortRange(networkSecurityGroupsDto.getSourceFromRange(), networkSecurityGroupsDto.getSourceToRange())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                //.toPortRange(networkSecurityGroupsDto.getDestinationFromRange(),networkSecurityGroupsDto.getDestinationToRange())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    }
                }

            } else if (networkSecurityGroupsDto.getBound().equalsIgnoreCase("Outbound")) {
                if (networkSecurityGroupsDto.isPermission()) {
                    if (networkSecurityGroupsDto.getSourceRange().equals("*") &&
                            !networkSecurityGroupsDto.getDestinationRange().equals("*")) {
                        networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .allowOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    }else if (!networkSecurityGroupsDto.getSourceRange().equals("*") &&
                            networkSecurityGroupsDto.getDestinationRange().equals("*")) {
                        networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .allowOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    }else if (networkSecurityGroupsDto.getSourceRange().equals("*") &&
                            networkSecurityGroupsDto.getDestinationRange().equals("*")) {
                        networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .allowOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    }else {
                        networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .allowOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                //.fromPortRange(networkSecurityGroupsDto.getSourceFromRange(), networkSecurityGroupsDto.getSourceToRange())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                //.toPortRange(networkSecurityGroupsDto.getDestinationFromRange(),networkSecurityGroupsDto.getDestinationToRange())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    }
                } else {
                    if (networkSecurityGroupsDto.getSourceRange().equals("*") &&
                            !networkSecurityGroupsDto.getDestinationRange().equals("*")) {
                        networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .denyOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    } else if (!networkSecurityGroupsDto.getSourceRange().equals("*") &&
                            networkSecurityGroupsDto.getDestinationRange().equals("*")) {
                        networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .denyOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    } else if (networkSecurityGroupsDto.getSourceRange().equals("*") &&
                            networkSecurityGroupsDto.getDestinationRange().equals("*")) {
                        networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .denyOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                .fromAnyPort()
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                .toAnyPort()
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    } else {
                        networkSecurityGroup.update()
                                .updateRule(networkSecurityGroupsDto.getRuleName())
                                .denyOutbound()
                                .fromAddress(networkSecurityGroupsDto.getFromAddress())
                                //.fromPortRange(networkSecurityGroupsDto.getSourceFromRange(), networkSecurityGroupsDto.getSourceToRange())
                                .fromPortRanges(networkSecurityGroupsDto.getSourceRange())
                                .toAddress(networkSecurityGroupsDto.getToAddress())
                                //.toPortRange(networkSecurityGroupsDto.getDestinationFromRange(),networkSecurityGroupsDto.getDestinationToRange())
                                .toPortRanges(networkSecurityGroupsDto.getDestinationRange())
                                .withProtocol(networkSecurityGroupsDto.getProtocol())
                                .withPriority(networkSecurityGroupsDto.getPriority())
                                .parent().apply();
                    }
                }

            }
        }catch (CloudException e) {
            logger.error("Error occurred :::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }


        return NetworkSecurityGroupsResponseDto.builder()
                .name(networkSecurityGroup.name())
                .id(networkSecurityGroup.id())
                .resourceGroupName(networkSecurityGroup.resourceGroupName())
                .tags(networkSecurityGroup.tags())
                .build();
    }

    @Override
    public List<Map<String,String>> getAssociatedSubnets(HttpServletRequest request, String resourceGroupName, String nsg) throws JSONException {
        NetworkSecurityGroup networkSecurityGroup;
        List<Map<String,String>> subnetList=new ArrayList<>();

        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));

            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            String id = "/subscriptions/" + credentials.getSubscriptionId() + "/resourceGroups/" + resourceGroupName + "/providers/Microsoft.Network/networkSecurityGroups/" + nsg;

            networkSecurityGroup = azure.networkSecurityGroups().getById(id);
            for(Subnet subnet:networkSecurityGroup.listAssociatedSubnets()){

                Map<String,String> map=new HashMap<>();
                map.put("Vnet",subnet.parent().name());
                map.put("Subnet",subnet.name());

                subnetList.add(map);

            }
        }catch (CloudException e) {
            logger.error("Error occurred :::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return subnetList;
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
