package com.azureAccelerator.service.impl;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.*;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.service.AzureSqlDBService;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.service.VaultService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AzureSqlDBServiceImpl implements AzureSqlDBService {
    private static final Logger logger = LogManager.getLogger(AzureSqlDBServiceImpl.class);
    private final ApplicationProperties applicationProperties;
    private final VaultService vaultService;
    private final UserService userService;


    @Autowired
    public AzureSqlDBServiceImpl(ApplicationProperties applicationProperties, VaultService vaultService, UserService userService) {
        this.applicationProperties = applicationProperties;
        this.vaultService = vaultService;
        this.userService = userService;
    }

    @Override
    public SqlServerResponseDto createSqlServer(HttpServletRequest request, SqlServerDto sqlServerDto) throws JSONException {
        try {

            Map<String,String> tags=new HashMap<>();
            if(null != sqlServerDto.getResourceGroupName())
                tags.put("resourceGroupName",sqlServerDto.getResourceGroupName());
            tags.put("name",sqlServerDto.getServerName());
            tags.put("application","azx");
            tags.put("Environment","dev");
            tags.put("project","cloud");

            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            logger.info("Creating Azure Sql Server");

            SqlServer sqlServer = azure.sqlServers()
                    .define(sqlServerDto.getServerName())
                    .withRegion(sqlServerDto.getLocation())
                    .withExistingResourceGroup(sqlServerDto.getResourceGroupName())
                    .withAdministratorLogin(sqlServerDto.getAdminUser())
                    .withAdministratorPassword(sqlServerDto.getAdminPassword())
                    .withTags(tags)
                    .create();
            logger.debug("sqlServer Id :"+sqlServer.id());
            logger.debug("sqlServer fully Qualified DomainName :"+sqlServer.fullyQualifiedDomainName());
            logger.info("Azure Sql server created successfully");
            return SqlServerResponseDto.builder()
                    .id(sqlServer.id())
                    .name(sqlServer.name())
                    .location(sqlServer.regionName())
                    .resourceGroupName(sqlServer.resourceGroupName())
                    .tags(sqlServer.tags())
                    .build();
        } catch (CloudException e) {
            logger.error("Error occurred while createSqlServer::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public List<SqlServerResponseDto> sqlServers(HttpServletRequest request,String resourceGroupName) throws JSONException {
        try {
            List<SqlServerResponseDto> sqlServerResponseDtoList = new ArrayList<>();
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure()
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            logger.info("getting all sql servers...");
            List<SqlServer> sqlServerList = azure.sqlServers()
                    .listByResourceGroup(resourceGroupName);
            logger.debug("sqlServer List :"+sqlServerList);

            sqlServerList.forEach(sqlServer -> {
                SqlServerResponseDto serverResponseDto = SqlServerResponseDto
                        .builder()
                        .id(sqlServer.id())
                        .name(sqlServer.name())
                        .location(sqlServer.regionName())
                        .resourceGroupName(sqlServer.resourceGroupName())
                        .tags(sqlServer.tags())
                        .build();
                logger.debug("sqlServer id :"+sqlServer.id());
                sqlServerResponseDtoList.add(serverResponseDto);
            });
            return sqlServerResponseDtoList;
        }catch (CloudException e) {
            logger.error("Error occurred while sqlServers:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public SqlDBResponseDto createSqlDB(HttpServletRequest request,SqlDBDto sqlDBDto) throws JSONException {
        try{
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            logger.info("Creating SQL db...");
            SqlServer sqlServer = azure.sqlServers()
                    .listByResourceGroup(sqlDBDto.getResourceGroupName())
                    .stream()
                    .filter(sqlServer1 -> sqlServer1.name().equalsIgnoreCase(sqlDBDto.getSqlServerName()))
                    .findFirst().orElse(null);

            SqlDatabase sqlDB = null;
            if (sqlServer != null) {
                if (sqlDBDto.isConfigurable()) {
                    logger.info("Creating SQL db with Basic service Objective and Basic edition");
                    sqlDB= sqlServer.databases()
                            .define(sqlDBDto.getSqlDBName())
                            .withServiceObjective(ServiceObjectiveName.BASIC)
                            .withTag("Application", "AZX")
                            //.withEdition(DatabaseEditions.BASIC)
                            .create();
                    logger.debug("sql Server id is :"+sqlServer.id());
                    logger.debug("sqlDB Id is :"+sqlDB.id());
                    logger.info("SQL DB created successfully with Basic service Objective and Basic edition");
                } else {
                    logger.info("Creating Azure Sql DB with Default Configuration");
                    sqlDB = sqlServer.databases().define(sqlDBDto.getSqlDBName()).create();
                    logger.info("Azure Sql DB created successfully with Default Configuration");
                }
            }

            if (sqlDB != null) {
                return SqlDBResponseDto.builder()
                        .id(sqlDB.id())
                        .name(sqlDB.name())
                        .location(sqlDB.regionName())
                        .resourceGroupName(sqlDB.resourceGroupName())
                        .build();
            }

        } catch (CloudException e) {
            logger.error("Error occurred while createSqlDB::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return null;
    }

    public String deleteSqlDBs(HttpServletRequest request,List<SqlDBDto> sqlDBDtos) throws JSONException {
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials()).withSubscription(credentials.getSubscriptionId());

            for(SqlDBDto sqlDBDto:sqlDBDtos){
                PagedList<SqlServer> sqlServerPagedList = azure.sqlServers().listByResourceGroup(sqlDBDto.getResourceGroupName());
                logger.debug("sql server List :"+sqlServerPagedList);
                for (SqlServer sqlServer : sqlServerPagedList) {
                    logger.info("SQL DB " + sqlDBDto.getSqlDBName() + " Deletion in Progress ");
                    sqlServer.databases().delete(sqlDBDto.getSqlDBName());
                    logger.info("SQL DB " + sqlDBDto.getSqlDBName() + " Deleted Successfully");
                }
            }

            return null;
        }catch (CloudException e) {
            logger.error("Error occured while deleteSqlDB:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String deleteSqlDB(HttpServletRequest request,SqlDBDto sqlDBDto) throws JSONException {
        try {
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure() // Initial an Azure.Configurable object
                    .authenticate(credentials.getApplicationTokenCredentials()).withSubscription(credentials.getSubscriptionId());

            PagedList<SqlServer> sqlServerPagedList = azure.sqlServers().listByResourceGroup(sqlDBDto.getResourceGroupName());
            logger.debug("sql server List :"+sqlServerPagedList);
            for (SqlServer sqlServer : sqlServerPagedList) {
                logger.info("SQL DB " + sqlDBDto.getSqlDBName() + " Deletion in Progress ");
                sqlServer.databases().delete(sqlDBDto.getSqlDBName());
                logger.info("SQL DB " + sqlDBDto.getSqlDBName() + " Deleted Successfully");
            }
            return null;
        }catch (CloudException e) {
            logger.error("Error occured while deleteSqlDB:::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<SqlDBResponseDto> sqlDBs(HttpServletRequest request,String sqlServerName,String resourceGroupName) throws JSONException {
        try {
            List<SqlDBResponseDto> sqlDBResponseDtoList = new ArrayList<>();
            AzureCredentials credentials = applicationTokenCredentials(request.getHeader("userName"));
            Azure azure = Azure.configure()
                    .authenticate(credentials.getApplicationTokenCredentials())
                    .withSubscription(credentials.getSubscriptionId());
            logger.info("getting all Sql DB's");
            logger.debug("getting all sql severs...");
            List<SqlServer> sqlServer = azure.sqlServers()
                    .listByResourceGroup(resourceGroupName)
                    .stream()
                    .filter(sqlServer1 -> sqlServer1.name().equalsIgnoreCase(sqlServerName))
                    .collect(Collectors.toList());
            sqlServer.forEach(
                    sqlServer1 -> sqlServer1.databases().list().forEach(sqlDb -> {
                        SqlDBResponseDto sqlDBResponseDto = SqlDBResponseDto.builder()
                                .id(sqlDb.id())
                                .name(sqlDb.name())
                                .location(sqlDb.regionName())
                                .resourceGroupName(sqlDb.resourceGroupName())
                                .build();
                        sqlDBResponseDtoList.add(sqlDBResponseDto);
                    }));
            logger.debug("sql DB List :"+sqlDBResponseDtoList);
            logger.info("getting all SQL DB's is ended...");
            return sqlDBResponseDtoList;
        }catch (CloudException e) {
            logger.error("Error occurred while sqlDBs::::"+e.getMessage());
            throw new AzureAcltrRuntimeException(
                    e.body().message(),
                    null,
                    e.body().message(),
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
                                secret.getClientId(), secret.getTenantId(), secret.getClientSecret(), AzureEnvironment.AZURE),
                        secret.getSubscriptionId());
    }
}
