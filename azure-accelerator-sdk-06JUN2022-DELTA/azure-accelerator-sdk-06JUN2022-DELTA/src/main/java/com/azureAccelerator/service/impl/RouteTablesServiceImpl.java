package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.AzureCredentials;
import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.dto.RouteTablesDto;
import com.azureAccelerator.dto.RouteTablesResponseDto;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.RouteTablesService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.RouteTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RouteTablesServiceImpl implements RouteTablesService {

    private static final Logger logger = LogManager.getLogger(RouteTablesServiceImpl.class);

    private final VaultService vaultService;
    private final ApplicationProperties applicationProperties;
    private final UserService userService;


    public RouteTablesServiceImpl(VaultService vaultService, ApplicationProperties applicationProperties, UserService userService) {
        this.vaultService = vaultService;
        this.applicationProperties = applicationProperties;
        this.userService = userService;
    }


    @Override
    public RouteTablesResponseDto createRouteTable(HttpServletRequest request, RouteTablesDto routeTablesDto) throws IOException {


        Map<String,String> tags=new HashMap<>();
        if(null != routeTablesDto.getResourceGroupName())
        tags.put("resourceGroupName",routeTablesDto.getResourceGroupName());
        tags.put("name",routeTablesDto.getRouteTableName());
        tags.put("application","azx");
        tags.put("Environment","dev");
        tags.put("project","cloud");

        RouteTable routeTable = null;
       try{


            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());

            routeTable=azure.routeTables().define(routeTablesDto.getRouteTableName())
                                  .withRegion(routeTablesDto.getRegion())
                                  .withExistingResourceGroup(routeTablesDto.getResourceGroupName())
                                  .withTags(tags)
                                  //.withRoute("", RouteNextHopType.VIRTUAL_NETWORK_GATEWAY)
                                  .create();

       } catch (CloudException e) {
           logger.error("Error occured while createRouteTable:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
         } catch (Exception e) {
           logger.error("Error occured while createRouteTable:::::"+e.getMessage());
       }



       return RouteTablesResponseDto.builder()
                .id(routeTable.id())
                .name(routeTable.name())
                .properties(routeTable.isBgpRoutePropagationDisabled()+"#"+routeTable.routes())
                .type(routeTable.type()+"#"+routeTable.listAssociatedSubnets()+"#"+routeTable.resourceGroupName())
                .region(routeTable.regionName()+"#"+routeTable.key()+"#"+routeTable.manager()+"#"+routeTable.inner())
                .tags(routeTable.tags())
                .build();

    }

    @Override
    public List<RouteTablesResponseDto> getRouteTables(HttpServletRequest request,String resourceGroupName) {

        List<RouteTablesResponseDto> routeTablesResponseDtoList = new ArrayList<>();
        try{
        AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
        Azure azure = Azure.configure()
                .authenticate(credentials.getApplicationTokenCredentials())
                .withSubscription(credentials.getSubscriptionId());
        
        System.out.println(credentials.getSubscriptionId());

        List<RouteTable> list;

        if(resourceGroupName != null && resourceGroupName.length() > 0)
        {
            list= new ArrayList(
                    azure.routeTables().listByResourceGroup(resourceGroupName));
        }else
        {
            list = new ArrayList(
                    azure.routeTables().list());
        }

        list.forEach(routeTable -> {
            routeTablesResponseDtoList.add(RouteTablesResponseDto.builder()
                    .id(routeTable.id())
                    .name(routeTable.name())
                    .properties(routeTable.isBgpRoutePropagationDisabled()+"#"+routeTable.routes())
                    .type(routeTable.type()+"#"+routeTable.listAssociatedSubnets()+"#"+routeTable.resourceGroupName())
                    .region(routeTable.regionName()+"#"+routeTable.key()+"#"+routeTable.manager()+"#"+routeTable.inner())
                    .tags(routeTable.tags())
                    .build());
        });
        } catch (
                CloudException e) {
            logger.error("Error occured while getRouteTables:::::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(e.body().message(), null, e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Error occured while getRouteTables:::::"+e.getMessage());
        }

        return routeTablesResponseDtoList;
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
