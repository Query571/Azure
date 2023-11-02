package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.AzureCredentials;
import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.IntegratingAKStoACRService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.azureAccelerator.util.GetToken;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.containerservice.KubernetesCluster;
import com.microsoft.azure.management.containerservice.ManagedClusterAddonProfile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IntegratingAKStoACRServiceImpl implements IntegratingAKStoACRService {

    private static final Logger logger = LogManager.getLogger(IntegratingAKStoACRServiceImpl.class);
    private final VaultService vaultService;
    private final ApplicationProperties applicationProperties;
    private final UserService userService;


    public IntegratingAKStoACRServiceImpl(VaultService vaultService, ApplicationProperties applicationProperties, UserService userService) {
        this.vaultService = vaultService;
        this.applicationProperties = applicationProperties;

        this.userService = userService;
    }


    @Override
    public String integratingAKStoACR(HttpServletRequest request, String resourceGroupName, String aksName, String acr) throws JSONException {

        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
        List<String> stringList= new ArrayList<>();

        try {
            logger.info("aks Integration Started...");

            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "integrateAcr.sh",
                    secret.getClientId(),
                    secret.getClientSecret(),
                    secret.getTenantId(),
                    resourceGroupName,aksName,acr).start();

            logger.debug("Integrating Aks to Acr Process Arguments :"+process);
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
                logger.debug(stringList);
                logger.info("Integration Ended");
                String line="";
                return String.join("",stringList);
            }
        } catch (Exception e) {
            logger.error("Error occured while integratingAKStoACR:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,e);
        }


    }

    @Override
    public String disIntegratingAKStoACR(HttpServletRequest request,String resourceGroupName, String aksName, String acr) {

        return null;
    }

    @Override
    public Map<String,String> importingDockerImages(HttpServletRequest request,String acrName, String imageName, String version) throws JSONException {
        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

        List<String> stringList= new ArrayList<>();
        Map<String,String> map=new HashMap<>();

        try {
            logger.info("importing dockerhub images Started...");

            ProcessBuilder processBuilder = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "importdockerimages.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),acrName,imageName,version);
            logger.info("cmdList "+processBuilder.command());

            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "importdockerimages.sh",
                    secret.getClientId(),
                    secret.getClientSecret(),
                    secret.getTenantId(),
                    acrName,imageName,version)
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
                logger.info("importing dockerhub images Ended...");
                String line="";
                while ((line=response.readLine())!=null) {
                    stringList.add(line);
                }
                logger.debug(stringList);
                map.put("status",String.join("",stringList));

                return map;
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

    @Override
    public Map<String, String> importingPrivateDockerhubImages(HttpServletRequest request, String acrName, String imageName, String version, String userName, String password) throws JSONException {
        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

        List<String> stringList= new ArrayList<>();
        Map<String,String> map=new HashMap<>();

        try {
            logger.info("importing dockerhub images Started...");

            ProcessBuilder processBuilder = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "importingprivatedockerhubimages.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),acrName,imageName,version,userName,password);
            logger.info("cmdList "+processBuilder.command());

            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "importingprivatedockerhubimages.sh",
                    secret.getClientId(),
                    secret.getClientSecret(),
                    secret.getTenantId(),
                    acrName,imageName,version,userName,password)
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
                logger.info("importing dockerhub images Ended...");
                String line="";
                while ((line=response.readLine())!=null) {
                    stringList.add(line);
                }
                logger.debug(stringList);
                map.put("status",String.join("",stringList));

                return map;
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

    @Override
    public Map<String,String> importingDockerImagesACRToACR(HttpServletRequest request,String acrResourceName, String destinationAcrName, String imageName, String version) throws JSONException {

        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

        List<String> stringList= new ArrayList<>();
        Map<String,String> map=new HashMap<>();

        try {
            logger.info("importing docker images acr to acr Starting...");

            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "importdockerimagesacrtoacr.sh",
                    secret.getClientId(),
                    secret.getClientSecret(),
                    secret.getTenantId(),
                    acrResourceName,destinationAcrName,imageName,version).start();
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
                logger.info("importing docker images acr to acr Ended...");
                String line="";
                while ((line=response.readLine())!=null) {
                    stringList.add(line);
                }
                logger.debug(stringList);
                map.put("status",String.join("",stringList));

                return map;
            }
        } catch (Exception e) {
            logger.error(" Exception : importingDockerImagesACRToACR:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String deleteDockerImagesInAcr(HttpServletRequest request,String acrName, String imageName) throws JSONException {
        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

        List<String> stringList= new ArrayList<>();

        try {

            logger.info("deleting dockerhub images in acr  Started...");

            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "deletedockerimagesinacr.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),acrName,imageName).start();
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
                logger.info("deleted dockerhub images in acr ...");
                String line="";
                while ((line=response.readLine())!=null) {
                    stringList.add(line);
                }
                logger.debug(stringList);

                return String.join("",stringList);
            }
        } catch (Exception e) {
            logger.error("Error occured while  fetching deleteDockerImagesInAcr:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public JSONObject checkNameAvailability(HttpServletRequest request,String acrName) throws JSONException {

        String output=null;
        String inputLine=null;
        StringBuffer response = new StringBuffer();
        JSONObject json = null;

        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));

            URL url = new URL("https://management.azure.com/" +
                    "subscriptions/"+credentials.getSubscriptionId()+"/providers/Microsoft.ContainerRegistry/checkNameAvailability?api-version=2019-05-01");

            GetToken getToken = new GetToken(vaultService, applicationProperties, userService);

            logger.info("url : " + url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "bearer "+getToken.gettingToken(request.getHeader("userName")));

            String properties = "{\n" +
                    "  \"name\": \""+acrName+"\",\n" +
                    "  \"type\": \"Microsoft.ContainerRegistry/registries\"\n" +
                    "}";
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
                Object obj = JSONValue.parse(response.toString());
                json = (org.json.simple.JSONObject) obj;
                in1.close();

            }
        }catch (IOException e) {
            logger.error("Error occurred while checkNameAvailability:::::"+e.getMessage());
        }
        return json;
    }

    @Override
    public Map<String,String> enableKeystoreForAks(HttpServletRequest request,String resourceGroupName, String aksName) throws JSONException {


        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

        List<String> stringList= new ArrayList<>();
        Map<String,String> map=new HashMap<>();

        try {
            logger.info("enable keys for aks Started...");

            ProcessBuilder processBuilder = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "enablekeystore.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),resourceGroupName,aksName);
            logger.info("cmdList "+processBuilder.command());

            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "enablekeystore.sh",
                    secret.getClientId(),
                    secret.getClientSecret(),
                    secret.getTenantId(),
                    resourceGroupName,aksName)
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
                logger.info("enable keys for aks Ended...");
                String line="";
                while ((line=response.readLine())!=null) {
                    stringList.add(line);
                }
                logger.debug(stringList);
                map.put("status","Enabled Successfully");

                return map;
            }
        } catch (Exception e) {
            logger.error("Error occurred while enable keys for aks::::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

       /* Map<String, ManagedClusterAddonProfile> addOnProfileMap;
        Map<String,String> map=new HashMap<>();

        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure()
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            logger.info("Enable KeystoreForAks is starting...");

            String aksId = "/subscriptions/" +
                    credentials.getSubscriptionId() +
                    "/resourcegroups/" +
                    resourceGroupName +
                    "/providers/Microsoft.ContainerService/managedClusters/" +
                    aksName;

            KubernetesCluster kubernetesCluster = azure.kubernetesClusters().getById(aksId);
            logger.debug("AKS ID :"+kubernetesCluster.id());
            addOnProfileMap = kubernetesCluster.addonProfiles();
            logger.debug("Before : addOnProfileMap" + addOnProfileMap.toString());

            addOnProfileMap.get("azureKeyvaultSecretsProvider").withEnabled(true);

            logger.debug("After : addOnProfileMap" + addOnProfileMap.toString());

            kubernetesCluster.update().withAddOnProfiles(addOnProfileMap).apply();
            map.put("Status","Enabled Successfully");
            logger.info("Enable KeystoreForAks is ended...");
        }catch(CloudException e ){
            logger.error("Exception: Enable Key for Aks : "+ e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,e);
        }
        return map;*/



    }

    @Override
    public Map<String,String> disableKeystoreForAks(HttpServletRequest request,String resourceGroupName, String aksName) throws JSONException {


        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

        List<String> stringList= new ArrayList<>();
        Map<String,String> map=new HashMap<>();

        try {
            logger.info("disable keys for aks Started...");

            ProcessBuilder processBuilder = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "disablekeystore.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),resourceGroupName,aksName);
            logger.info("cmdList "+processBuilder.command());

            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "disablekeystore.sh",
                    secret.getClientId(),
                    secret.getClientSecret(),
                    secret.getTenantId(),
                    resourceGroupName,aksName)
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
                logger.info("disable keys for aks Ended...");
                String line="";
                while ((line=response.readLine())!=null) {
                    stringList.add(line);
                }
                logger.debug(stringList);
                map.put("status","disabled Successfully");

                return map;
            }
        } catch (Exception e) {
            logger.error("Error occurred while disable keys for aks ::::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        /*Map<String, ManagedClusterAddonProfile> addOnProfileMap;
        Map<String,String> map=new HashMap<>();
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure()
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            logger.info("Disable KeystoreForAks is starting...");

            String aksId = "/subscriptions/" +
                    credentials.getSubscriptionId() +
                    "/resourcegroups/" +
                    resourceGroupName +
                    "/providers/Microsoft.ContainerService/managedClusters/" +
                    aksName;

            KubernetesCluster kubernetesCluster = azure.kubernetesClusters().getById(aksId);
            logger.info("kubernetes >>>" +kubernetesCluster.name());


            addOnProfileMap = kubernetesCluster.addonProfiles();
            logger.info("Before : addOnProfileMap" + addOnProfileMap.toString());

            addOnProfileMap.get("azureKeyVaultSecretsProvider").withEnabled(false);

            logger.info("After : addOnProfileMap" + addOnProfileMap.toString());

            kubernetesCluster.update().withAddOnProfiles(addOnProfileMap).apply();
            map.put("status","Disabled Successfully");
            logger.info("Disable KeystoreForAks is ended...");
        }catch(CloudException e ){
            logger.error("Exception: disable key store for AKS Store : "+ e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,e);
        }

        return map;*/


    }
    @Override
    public JSONObject CheckingAzureKeyVaultSecretsProvider(HttpServletRequest request,String resourceGroupName,String aksName) throws JSONException {

        String output=null;
        String inputLine=null;
        StringBuffer response = new StringBuffer();
        JSONObject json = null;

        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));

            URL url = new URL("https://management.azure.com/subscriptions/"+credentials.getSubscriptionId()+"/resourceGroups/"+resourceGroupName+"/providers/Microsoft.ContainerService/managedClusters/"+aksName+"?api-version=2022-02-01");

            GetToken getToken = new GetToken(vaultService, applicationProperties, userService);

            logger.info("url : " + url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            conn.setRequestProperty("Authorization", "bearer "+getToken.gettingToken(request.getHeader("userName")));

            logger.info(conn.getResponseCode() + "-----" + conn.getResponseMessage());

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK ||conn.getResponseCode() == HttpURLConnection.HTTP_CREATED||conn.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED) { //success

                BufferedReader in1 = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));

                while ((inputLine = in1.readLine()) != null) {
                    response.append(inputLine);
                }

                Object obj = JSONValue.parse(response.toString());
                json = (org.json.simple.JSONObject) obj;
                logger.debug(json);
                in1.close();

            }
        }catch (IOException e) {
            logger.error("exception: CheckingAzureKeyVaultSecretsProvider:::::"+e.getMessage());
        }
        return json;
    }

    @Override
    public Object getImageTag(HttpServletRequest request,String acrName, String image) throws JSONException {
        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());

        List<String> stringList= new ArrayList<>();

        try {

            logger.info("getImageTag dockerhub images in acr  Started...");

            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "acrRepotag.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),acrName,image).start();
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
                logger.info("getImageTag dockerhub images in acr ...");
                String line="";
                while ((line=response.readLine())!=null) {
                    stringList.add(line);
                }
                logger.debug(stringList);
                org.json.simple.JSONArray obj = (org.json.simple.JSONArray) JSONValue.parse(stringList.toString());


                return obj.get(0);
            }
        } catch (Exception e) {
            logger.error("Error occured while getImageTag:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String dashboardDeploy(HttpServletRequest request,String rg, String aksName) throws JSONException {
        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
        List<String> stringList= new ArrayList<>();

        try {
            logger.info("aks dashboard Started...");

            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "aksDashboardDeploy.sh",
                    secret.getClientId(),
                    secret.getClientSecret(),
                    secret.getTenantId(),
                    rg,aksName,aksName).start();

            logger.debug("Integrating Aks to Acr Process Arguments :"+process);
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
                logger.debug(stringList);
                logger.info("dashboard Ended");
                String line="";
                return String.join("",stringList);
            }
        } catch (Exception e) {
            logger.error("Error occurred while integratingAKStoACR:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,e);
        }

    }

    @Override
    public List<String> listTags(HttpServletRequest request, String image) throws Exception {

        List<String> tags=new ArrayList<>();

        if(image.equalsIgnoreCase("Ubuntu")){
            tags.add("latest");tags.add("bionic-20220902");tags.add("bionic");tags.add("18.04");tags.add("rooling");tags.add("kinetic-20220830");
            tags.add("jammy-20220815");tags.add("kinetic");tags.add("devel");tags.add("focal-20220826");
        }else if (image.equalsIgnoreCase("Redis")){
            tags.add("latest");tags.add("bullseye");tags.add("alpine3.16");tags.add("alpine");tags.add("7.0.5-bullseye");tags.add("7.0.5-alpine3.16");
            tags.add("7.0.5-alpine");tags.add("7.0.5");tags.add("7.0-alpine3.16");tags.add("7.0-alpine");
        }else if(image.equalsIgnoreCase("Nginx")){
            tags.add("latest");tags.add("stable-perl");tags.add("stable");tags.add("perl");tags.add("mainline-perl");tags.add("mainline");
            tags.add("1.23.1-perl");tags.add("1.23.1");tags.add("1.23-perl");tags.add("1.23");
        }else if(image.equalsIgnoreCase("MySQL")){
            tags.add("latest");tags.add("5.7.39-oracle");tags.add("5.7.39");tags.add("5.7-oracle");tags.add("5.7");tags.add("5-oracle");
            tags.add("5");tags.add("oracle");tags.add("8.0.30-oracle");tags.add("8.0.30");
        }else if(image.equalsIgnoreCase("Postgres")){
            tags.add("latest");tags.add("bullseye");tags.add("15beta4-bullseye");tags.add("15beta4");tags.add("14.5-bullseye");tags.add("14.5");
            tags.add("14-bullseye");tags.add("14");tags.add("13.8-bullseye");tags.add("13.8");
        }else if(image.equalsIgnoreCase("mongo")){
            tags.add("latest");tags.add("4.4.17-rc2-windowsservercore-ltsc2022");tags.add("4.2-rc-nanoserver");tags.add("4.4.17-rc2-windowsservercore");tags.add("4.4.17-rc2-nanoserver-ltsc2022");tags.add("4.4.17-rc2-nanoserver-1809");
            tags.add("4.4.17-rc2-nanoserver");tags.add("4.4.17-rc2");tags.add("4.4-rc-windowsservercore-ltsc2022");tags.add("4.4-rc-windowsservercore-1809");
        }else if(image.equalsIgnoreCase("httpd")){
            tags.add("latest");tags.add("bullseye");tags.add("2.4.54-bullseye");tags.add("2.4.54");tags.add("2.4");tags.add("2.4-bullseye");
            tags.add("2-bullseye");tags.add("2");tags.add("alpine3.16");tags.add("alpine");
        }else if(image.equalsIgnoreCase("SonarQube")){
            tags.add("latest");tags.add("enterprise");tags.add("developer");tags.add("datacenter-search");tags.add("datacenter-app");tags.add("community");
            tags.add("9.6.1-enterprise");tags.add("9.6.1-developer");tags.add("9.6.1-datacenter-search");tags.add("9.6.1-datacenter-app");
        }else if(image.equalsIgnoreCase("Ruby")){
            tags.add("latest");tags.add("slim-buster");tags.add("slim-bullseye");tags.add("slim");tags.add("buster");tags.add("bullseye");
            tags.add("3.2.0-preview2-slim-buster");tags.add("3.2.0-preview2-slim-bullseye");tags.add("3.2.0-preview2-slim");tags.add("3.2.0-preview2-buster");
        }else if(image.equalsIgnoreCase("Tomcat")){
            tags.add("latest"); tags.add("8.5.82-jdk8-corretto-al2");tags.add("8.5.82-jdk8-corretto");tags.add("8.5.82-jdk17-corretto-al2");tags.add("8.5.82-jdk17-corretto");tags.add("8.5.82-jdk11-corretto-al2");tags.add("8.5.82-jdk11-corretto");
            tags.add("8.5-jdk11-corretto");tags.add("8-jdk8-corretto");tags.add("8-jdk17-corretto-al2");tags.add("8-jdk17-corretto");
        }else if(image.equalsIgnoreCase("Neo4J")){
            tags.add("latest");tags.add("enterprise");tags.add("community");tags.add("4.4.9-enterprise");tags.add("4.4.9-community");tags.add("4.4.9");
            tags.add("4.4.11-enterprise");tags.add("4.4.11-community");tags.add("4.4.11");tags.add("4.4.10-enterprise");
        }else if(image.equalsIgnoreCase("Elasticsearch")){
            tags.add("8.4.2");tags.add("8.4.1");tags.add("8.4.0");tags.add("7.17.6");tags.add("8.3.3");tags.add("8.3.2");
            tags.add("8.3.1");tags.add("7.17.5");tags.add("8.2.2");tags.add("8.2.1");
        }else if(image.equalsIgnoreCase("maven")){
           tags.add("latest"); tags.add("amazoncorretto");tags.add("3.8.6-amazoncorretto-8");tags.add("3.8.6-amazoncorretto-18");tags.add("3.8.6-amazoncorretto-17");tags.add("3.8.6-amazoncorretto-11");tags.add("3.8.6-amazoncorretto");
            tags.add("3.8-amazoncorretto-8");tags.add("3.8-amazoncorretto-18");tags.add("3.8-amazoncorretto-17");tags.add("3.8-amazoncorretto-11");
        }else if(image.equalsIgnoreCase("Joomla")){
            tags.add("latest");tags.add("php8.1-fpm");tags.add("4.2.2-php8.1-fpm");tags.add("4-php8.1-fpm");tags.add("3.10.11-php7.4-fpm");tags.add("3.10-php7.4-fpm");
            tags.add("3-php7.4-fpm");tags.add("php8.1-apache");tags.add("4.2.2-php8.1-apache");tags.add("4.2-php8.1-apache");
        }else if(image.equalsIgnoreCase("Django")){
            tags.add("latest");tags.add("onbuild");tags.add("python3-onbuild");tags.add("python2-onbuild");tags.add("1");tags.add("1.10");
            tags.add("1.10.4");tags.add("python3");tags.add("1-python3");tags.add("1.10-python3");
        }else if(image.equalsIgnoreCase("Mariadb")){
            tags.add("latest");tags.add("jammy");tags.add("10.9.3-jammy");tags.add("10.9.3");tags.add("10.9-jammy");tags.add("10.9");
            tags.add("10.8.5-jammy");tags.add("10.8.5");tags.add("10.8-jammy");tags.add("10.8");
        }else if(image.equalsIgnoreCase("Wordpress")){
            tags.add("latest"); tags.add("beta-php8.1-fpm-alpine");tags.add("beta-php8.1-fpm");tags.add("beta-php8.1-apache");tags.add("beta-php8.0-fpm-alpine");tags.add("beta-php8.0-fpm");tags.add("beta-6.1-php8.1-fpm-alpine");
            tags.add("beta-6.1-php8.1-fpm");tags.add("beta-6.1-php8.1-apache");tags.add("beta-6.1-php8.1");tags.add("beta-6.1-php8.0-fpm-alpine");
        }else if(image.equalsIgnoreCase("Arangodb")){
            tags.add("latest");tags.add("3.9.3");tags.add("");tags.add("3.9");tags.add("3.9.2");tags.add("3.8.7");
            tags.add("3.8");tags.add("3.7.18");tags.add("3.7");tags.add("3.9.1");
        }else if(image.equalsIgnoreCase("Memcached")){
            tags.add("latest");tags.add("bullseye");tags.add("1.6.17-bullseye");tags.add("1.6.17");tags.add("1.6-bullseye");tags.add("1.6");
            tags.add("1-bullseye");tags.add("1");tags.add("alpine3.16");tags.add("alpine");
        }else if(image.equalsIgnoreCase("Centos")){
            tags.add("latest");tags.add("centos7.9.2009");tags.add("centos7");tags.add("7.9.2009");tags.add("7");tags.add("centos8.4.2105");
            tags.add("centos8");tags.add("centos6.10");tags.add("centos6");tags.add("8.4.2105");
        }else if(image.equalsIgnoreCase("Fedora")){
            tags.add("latest");tags.add("36");tags.add("rawhide");tags.add("37");tags.add("35");tags.add("34");
            tags.add("33");tags.add("32");tags.add("31");tags.add("30");
        }else if(image.equalsIgnoreCase("RethinkDB")){
            tags.add("latest");tags.add("bullseye-slim");tags.add("2.4.2-bullseye-slim");tags.add("2.4.2");tags.add("2.4-bullseye-slim");tags.add("2.4");
            tags.add("2-bullseye-slim");tags.add("2");tags.add("buster-slim");tags.add("2.4.1-buster-slim");
        }else if(image.equalsIgnoreCase("Sentry")){
            tags.add("latest");tags.add("onbuild");tags.add("9.1.2-onbuild");tags.add("9.1.2");tags.add("9.1-onbuild");tags.add("9.1");
            tags.add("9-onbuild");tags.add("9");tags.add("9.1.1-onbuild");tags.add("9.1.1");
        }else if(image.equalsIgnoreCase("Owncloud")){
            tags.add("latest");tags.add("9-fpm");tags.add("9.1-fpm");tags.add("9.1.8-fpm");tags.add("9");tags.add("9.1");
            tags.add("9.1.8");tags.add("9-apache");tags.add("9.1-apache");tags.add("9.1.8-apache");
        }else if(image.equalsIgnoreCase("Jenkins")){
            tags.add("latest");tags.add("2.60.3");tags.add("2.60.3-alpine");tags.add("2.60.2");tags.add("2.60.2-alpine");tags.add("2.60.1");
            tags.add("2.46.3-alpine");tags.add("2.46.3");tags.add("2.46.2");tags.add("2.46.2-alpine");
        }else {
            throw new Exception("dockerhub image is not available in your list");
        }

        return tags;
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
