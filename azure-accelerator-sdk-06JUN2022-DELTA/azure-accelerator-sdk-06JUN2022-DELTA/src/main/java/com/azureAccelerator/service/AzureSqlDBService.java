package com.azureAccelerator.service;

import com.azureAccelerator.dto.SqlDBDto;
import com.azureAccelerator.dto.SqlDBResponseDto;
import com.azureAccelerator.dto.SqlServerDto;
import com.azureAccelerator.dto.SqlServerResponseDto;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface AzureSqlDBService {

    SqlServerResponseDto createSqlServer(HttpServletRequest request, SqlServerDto sqlServerDto) throws JSONException;

    List<SqlServerResponseDto> sqlServers(HttpServletRequest request,String resourceGroupName) throws JSONException;

    SqlDBResponseDto createSqlDB(HttpServletRequest request,SqlDBDto sqlDBDto) throws JSONException;

    public String deleteSqlDBs(HttpServletRequest request,List<SqlDBDto> sqlDBDtos) throws JSONException;

    String deleteSqlDB(HttpServletRequest request,SqlDBDto sqlDBDto) throws JSONException;

    List<SqlDBResponseDto> sqlDBs(HttpServletRequest request,String sqlServerName, String resourceGroupName) throws JSONException;
}
