package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.AKSDeployService;
import com.azureAccelerator.service.ARMTemplateService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
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
import java.util.List;

@Service
public class ARMTemplateServiceImpl implements ARMTemplateService {

    private static final Logger logger = LogManager.getLogger(ARMTemplateServiceImpl.class);
    private final VaultService vaultService;
    private final ApplicationProperties applicationProperties;
    private AKSDeployService aksDeployService;
    private final UserService userService;


    public ARMTemplateServiceImpl(VaultService vaultService, ApplicationProperties applicationProperties, AKSDeployService aksDeployService, UserService userService) {
        this.vaultService = vaultService;
        this.applicationProperties = applicationProperties;
        this.aksDeployService = aksDeployService;
        this.userService = userService;
    }

    @Override
    public Object getVnetARMTemplate(HttpServletRequest request, String resourceGroup, String vNet) throws JSONException {

        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
        logger.info("Vnet ARM Template downloading is started...");
        List<String> stringList= new ArrayList<>();
        String vNetId=" /subscriptions/"+secret.getSubscriptionId()+"/resourceGroups/"+resourceGroup+"/providers/Microsoft.Network/virtualNetworks/"+vNet;
        logger.debug("Vnet Id :"+vNetId);
        try{
        ProcessBuilder processBuilder = new
                ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "ARMtemplate.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),resourceGroup,vNetId);
        logger.debug("cmdList "+processBuilder.command());

        Process process = new
                ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "ARMtemplate.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),resourceGroup,vNetId).start();
        logger.info("After Process");
            BufferedReader Reader = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            if (process.waitFor() != 0) {
                logger.info("Error Reader.readLine() ::"+Reader.readLine());
                throw new AzureAcltrRuntimeException(
                        Reader.readLine(),
                        null,
                        Reader.readLine(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                BufferedReader response = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));

                String line="";
                while ((line=response.readLine())!=null) {
                    logger.info("Reading line for response..."+stringList);
                    stringList.add(line);
                }
                org.json.simple.JSONArray obj = (org.json.simple.JSONArray) JSONValue.parse(stringList.toString());
                logger.info("Vnet ARM Template downloading is Ended...");
                return obj.get(0);
            }

    } catch (Exception e) {
        logger.error("Exception :: "+ e.getMessage());
        throw new AzureAcltrRuntimeException(
                e.getMessage(),
                null,
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    }

    @Override
    public Object getKeyVaultARMTemplate(HttpServletRequest request,String resourceGroup, String keyVault) throws JSONException {


        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
        List<String> stringList= new ArrayList<>();
        String keyVaultId=" /subscriptions/"+secret.getSubscriptionId()+"/resourceGroups/"+resourceGroup+"/providers/Microsoft.KeyVault/vaults/"+keyVault;
        try{

            ProcessBuilder processBuilder = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "ARMtemplate.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),resourceGroup,keyVaultId);
            logger.info("cmdList "+processBuilder.command());

            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "ARMtemplate.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),resourceGroup,keyVaultId).start();
            logger.info("After Process");
            BufferedReader Reader = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            if (process.waitFor() != 0) {
                logger.info("Error Reader.readLine() ::"+Reader.readLine());
                throw new AzureAcltrRuntimeException(
                        Reader.readLine(),
                        null,
                        Reader.readLine(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                BufferedReader response = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));

                String line="";
                while ((line=response.readLine())!=null) {
                    logger.info("Reading line for response..."+stringList);
                    stringList.add(line);
                }
                org.json.simple.JSONArray obj = (org.json.simple.JSONArray) JSONValue.parse(stringList.toString());


                return obj.get(0);
            }

        } catch (Exception e) {
            logger.error("Exception :: "+ e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Object getVMARMTemplate(HttpServletRequest request,String resourceGroup, String vm) throws JSONException {

        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
        List<String> stringList= new ArrayList<>();
        String vmId="/subscriptions/"+secret.getSubscriptionId()+"/resourceGroups/"+resourceGroup+"/providers/Microsoft.Compute/virtualMachines/"+vm;
        try{

            ProcessBuilder processBuilder = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "ARMtemplate.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),resourceGroup,vmId);
            logger.info("cmdList "+processBuilder.command());

            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "ARMtemplate.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),resourceGroup,vmId).start();
            logger.info("After Process");
            BufferedReader Reader = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            if (process.waitFor() != 0) {
                logger.info("Error Reader.readLine() ::"+Reader.readLine());
                throw new AzureAcltrRuntimeException(
                        Reader.readLine(),
                        null,
                        Reader.readLine(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                BufferedReader response = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));

                String line="";
                while ((line=response.readLine())!=null) {
                    logger.info("Reading line for response..."+stringList);
                    stringList.add(line);
                }
                org.json.simple.JSONArray obj = (org.json.simple.JSONArray) JSONValue.parse(stringList.toString());


                return obj.get(0);
            }

        } catch (Exception e) {
            logger.error("Exception :  : "+ e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public Object getNSGARMTemplate(HttpServletRequest request,String resourceGroup, String nsg) throws JSONException {

        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
        List<String> stringList= new ArrayList<>();
        String nsgId="/subscriptions/"+secret.getSubscriptionId()+"/resourceGroups/"+resourceGroup+"/providers/Microsoft.Network/networkSecurityGroups/"+nsg;
        try{

            ProcessBuilder processBuilder = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "ARMtemplate.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),resourceGroup,nsgId);
            logger.info("cmdList "+processBuilder.command());

            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "ARMtemplate.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),resourceGroup,nsgId).start();
            logger.info("After Process");
            BufferedReader Reader = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            if (process.waitFor() != 0) {
                logger.info("Error Reader.readLine() ::"+Reader.readLine());
                throw new AzureAcltrRuntimeException(
                        Reader.readLine(),
                        null,
                        Reader.readLine(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                BufferedReader response = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));

                String line="";
                while ((line=response.readLine())!=null) {
                    logger.info("Reading line for response..."+stringList);
                    stringList.add(line);
                }
                org.json.simple.JSONArray obj = (org.json.simple.JSONArray) JSONValue.parse(stringList.toString());


                return obj.get(0);
            }

        } catch (Exception e) {
            logger.error("Exception :  : "+ e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public Object getACRARMTemplate(HttpServletRequest request,String resourceGroup, String acr) throws JSONException {

        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));

        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
        List<String> stringList= new ArrayList<>();
        String acrId="/subscriptions/"+secret.getSubscriptionId()+"/resourceGroups/"+resourceGroup+"/providers/Microsoft.ContainerRegistry/registries/"+acr;
        try{

            ProcessBuilder processBuilder = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "ARMtemplate.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),resourceGroup,acrId);
            logger.info("cmdList "+processBuilder.command());

            Process process = new
                    ProcessBuilder("bash",applicationProperties.getAppScriptPath() + "ARMtemplate.sh",secret.getClientId(),secret.getClientSecret(),secret.getTenantId(),resourceGroup,acrId).start();
            logger.info("After Process");
            BufferedReader Reader = new BufferedReader(new InputStreamReader(
                    process.getErrorStream()));
            if (process.waitFor() != 0) {
                logger.info("Error Reader.readLine() ::"+Reader.readLine());
                throw new AzureAcltrRuntimeException(
                        Reader.readLine(),
                        null,
                        Reader.readLine(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                BufferedReader response = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));

                String line="";
                while ((line=response.readLine())!=null) {
                    logger.info("Reading line for response..."+stringList);
                    stringList.add(line);
                }
                org.json.simple.JSONArray obj = (org.json.simple.JSONArray) JSONValue.parse(stringList.toString());


                return obj.get(0);
            }

        } catch (Exception e) {
            logger.error("Exception : : "+ e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
