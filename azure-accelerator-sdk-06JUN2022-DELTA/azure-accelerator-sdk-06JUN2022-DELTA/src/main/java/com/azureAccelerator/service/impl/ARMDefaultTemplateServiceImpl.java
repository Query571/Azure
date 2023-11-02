package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.AzureCredentials;
import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.repository.AzureInstanceTypesRepository;
import com.azureAccelerator.service.ARMDefaultTemplateService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class ARMDefaultTemplateServiceImpl implements ARMDefaultTemplateService {

    private static final Logger logger = LogManager.getLogger(ARMDefaultTemplateServiceImpl.class);
    private final VaultService vaultService;
    private final UserService userService;

    @Autowired
    ResourceLoader resourceLoader;

    public ARMDefaultTemplateServiceImpl(VaultService vaultService,  UserService userService) {
        this.vaultService = vaultService;
        this.userService = userService;
    }
    @Override
    public Object defaultJsonTypeARM(HttpServletRequest request, String templateName, int count) throws Exception {

        List<LocalUserDto> userDtoList=userService.findByName(request.getHeader("userName"));
        var secret =
                vaultService.getSecrets(userDtoList.get(0).getSubscriptionId());
        Resource resource = null;
        logger.info("Default ARM template downloading is Starting...");

        if(templateName.equalsIgnoreCase("KeyVault")) {
            logger.debug("Default KeyVault ARM Template is downloading");
            resource = resourceLoader.getResource("classpath:TemplateJson/Vault.json");
            try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
                JSONParser parser = new JSONParser();
                Object template = parser.parse(inputStreamReader);
                JSONObject json = new JSONObject(template.toString());
                json.getJSONArray("resources").getJSONObject(0).getJSONObject("properties").put("tenantId",secret.getTenantId());
                json.getJSONArray("resources").getJSONObject(0).getJSONObject("properties").getJSONArray("accessPolicies").getJSONObject(0).put("objectId",secret.getObjectId());
                json.getJSONArray("resources").getJSONObject(0).getJSONObject("properties").getJSONArray("accessPolicies").getJSONObject(0).put("tenantId",secret.getTenantId());
                json.getJSONArray("resources").getJSONObject(0).getJSONObject("copy").put("count",count);
//                JSONObject json2 = new JSONObject(template.toString());
//                json2.
                String temp=json.toString();
                Object templateOfARM = parser.parse(temp);
                logger.debug("templateOfARM :"+templateOfARM);
                logger.info("Default ARM template downloading is Ended...");
                return JSONValue.parse(temp);
            } catch (Exception e) {
                throw new AzureAcltrRuntimeException(
                        e.getMessage(),
                        null,
                        e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else if(templateName.equalsIgnoreCase("Cluster")) {
            logger.debug("Default cluster ARM Template is downloading");
            resource = resourceLoader.getResource("classpath:TemplateJson/AKSTemplate.json");
        }
        else if(templateName.equalsIgnoreCase("SqlServer")){
            logger.debug("Default SqlServer ARM Template is downloading");
            resource = resourceLoader.getResource("classpath:TemplateJson/SQLServer.json");
        }else if (templateName.equalsIgnoreCase("VirtualNetwork")){
            logger.debug("Default VirtualNetwork ARM Template is downloading");
            resource = resourceLoader.getResource("classpath:TemplateJson/VirtualNetwork.json");
        }else if(templateName.equalsIgnoreCase("StorageAccount")){
            logger.debug("Default StorageAccount ARM Template is downloading");
            resource = resourceLoader.getResource("classpath:TemplateJson/StorageAccount.json");
        }else if(templateName.equalsIgnoreCase("SqlDb")){
            logger.debug("Default StorageAccount ARM Template is downloading");
            resource = resourceLoader.getResource("classpath:TemplateJson/SqlDb.json");
        }else if(templateName.equalsIgnoreCase("NetworkSecurityGroup")){
            logger.debug("Default StorageAccount ARM Template is downloading");
            resource = resourceLoader.getResource("classpath:TemplateJson/NetworkSecurityGroup.json");
        }else{
            throw new Exception("given wrong resources name");
        }
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            JSONParser parser = new JSONParser();
            Object template = parser.parse(inputStreamReader);
            JSONObject json = new JSONObject(template.toString());
            json.getJSONArray("resources").getJSONObject(0).getJSONObject("copy").put("count",count);
            String temp=json.toString();
            Object templateOfARM = parser.parse(temp);
            logger.debug("templateOfARM :"+templateOfARM);
            logger.info("Default ARM template downloading is Ended...");
            return JSONValue.parse(temp);
        } catch (Exception e) {
            throw new AzureAcltrRuntimeException(
                            e.getMessage(),
                            null,
                            e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
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
