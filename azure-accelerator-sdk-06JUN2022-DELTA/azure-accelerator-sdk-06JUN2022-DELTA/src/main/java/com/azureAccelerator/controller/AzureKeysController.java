package com.azureAccelerator.controller;


import com.azure.core.annotation.Post;
import com.azure.core.credential.TokenRequestContext;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyType;

import com.azureAccelerator.dto.ImportKeyDto;
import com.azureAccelerator.dto.KeysDto;
import com.azureAccelerator.dto.KeysResponseDto;
import com.azureAccelerator.service.AzureKeysService;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class AzureKeysController {
    private AzureKeysService azureKeysService;

    public AzureKeysController(AzureKeysService azureKeysService){

        this.azureKeysService=azureKeysService;
    }
    @PostMapping("createKey")
    public ResponseEntity<KeysResponseDto> createKeys(HttpServletRequest request, @RequestBody KeysDto keysDto) throws Exception {

        return new ResponseEntity<KeysResponseDto>(azureKeysService.createKeys(request,keysDto), HttpStatus.CREATED);
    }

    @GetMapping("getKeys")
    public ResponseEntity<Object>getSecret(HttpServletRequest request,@RequestParam  String keyVaultName) throws Exception {

        return  new ResponseEntity<Object>(azureKeysService.getKeys(request,keyVaultName),HttpStatus.OK);
    }

    @DeleteMapping("deleteKeys")
    public ResponseEntity<Map<String,String>> deleteKey(HttpServletRequest request,@RequestParam String keyName,@RequestParam String keyVaultName) throws JSONException {

        return new ResponseEntity<Map<String,String>>(azureKeysService.deleteKey(request,keyName,keyVaultName),HttpStatus.OK);
    }

    @PostMapping("importKey")
    public ResponseEntity<String> importKey(@RequestBody ImportKeyDto importKey) throws Exception {
        return new ResponseEntity<String>(azureKeysService.importKey(importKey),HttpStatus.OK);
    }

}
