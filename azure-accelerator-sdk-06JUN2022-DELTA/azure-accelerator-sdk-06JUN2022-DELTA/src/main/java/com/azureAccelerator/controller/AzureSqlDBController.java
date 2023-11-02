package com.azureAccelerator.controller;

import com.azureAccelerator.dto.SqlDBDto;
import com.azureAccelerator.dto.SqlDBResponseDto;
import com.azureAccelerator.dto.SqlServerDto;
import com.azureAccelerator.dto.SqlServerResponseDto;
import com.azureAccelerator.service.AzureSqlDBService;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
public class AzureSqlDBController {

    private final AzureSqlDBService azureSqlDBService;

    public AzureSqlDBController(AzureSqlDBService azureSqlDBService) {
        this.azureSqlDBService = azureSqlDBService;
    }

    @PostMapping("createSqlServer")
    public ResponseEntity<SqlServerResponseDto> createSqlServer(HttpServletRequest request,@RequestBody SqlServerDto sqlServerDto) throws IOException, JSONException {

         return new ResponseEntity<>(
                azureSqlDBService.createSqlServer(request,sqlServerDto), HttpStatus.OK);
    }

    @GetMapping("sqlServers")
    public ResponseEntity<List<SqlServerResponseDto>> sqlServers(HttpServletRequest request,@RequestParam String resourceGroupName) throws JSONException {

        return new ResponseEntity<>(
                azureSqlDBService.sqlServers(request,resourceGroupName),HttpStatus.OK);
    }

    @PostMapping("createSqlDB")
    public ResponseEntity<SqlDBResponseDto> createSqlDB(HttpServletRequest request,@RequestBody SqlDBDto sqlDBDto) throws JSONException {

        return new ResponseEntity(
                azureSqlDBService.createSqlDB(request,sqlDBDto),HttpStatus.OK);
    }

    @GetMapping("sqlDBs")
    public ResponseEntity<List<SqlDBResponseDto>> sqlDBs(HttpServletRequest request,@RequestParam String sqlServerName,
            @RequestParam String resourceGroupName) throws JSONException {

        return new ResponseEntity<>(
                azureSqlDBService.sqlDBs(request,sqlServerName,resourceGroupName),HttpStatus.OK);
    }

    @DeleteMapping("deleteSqlDBs")
    public ResponseEntity<String> deleteSqlDBs(HttpServletRequest request, @RequestBody List<SqlDBDto> sqlDBDtos) throws JSONException {

        return new ResponseEntity(
                azureSqlDBService.deleteSqlDBs(request,sqlDBDtos),HttpStatus.OK);
    }

    @PostMapping("deleteSqlDB")
    public ResponseEntity<String> deleteSqlDB(HttpServletRequest request, @RequestBody SqlDBDto sqlDBDto) throws JSONException {

        return new ResponseEntity(
                azureSqlDBService.deleteSqlDB(request,sqlDBDto),HttpStatus.OK);
    }

}
