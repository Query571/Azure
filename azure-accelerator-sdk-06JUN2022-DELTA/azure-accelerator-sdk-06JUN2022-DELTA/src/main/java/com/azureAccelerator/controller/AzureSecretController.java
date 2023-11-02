package com.azureAccelerator.controller;

//import com.azureAccelerator.dto.AzureScretResponseDto;
import com.azureAccelerator.dto.AzureSecretDto;
import com.azureAccelerator.dto.AzureSecretResponseDto;
import com.azureAccelerator.service.AzureSecretService;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class AzureSecretController {


    private final AzureSecretService azureSecretService;

    public AzureSecretController(AzureSecretService azureSecretService) {
        this.azureSecretService = azureSecretService;
    }

    @PostMapping("createKeyVaultSecretKey")
    ResponseEntity<AzureSecretResponseDto> createSecretKey(HttpServletRequest request, @RequestBody AzureSecretDto azureSecretDto) throws Exception {

        return new ResponseEntity<AzureSecretResponseDto>(
                azureSecretService.createSecretKey(request,azureSecretDto),HttpStatus.CREATED);
    }
    @GetMapping("getKeyVaultSecretKey")
    public ResponseEntity<Object> getSecret(HttpServletRequest request,@RequestParam  String keyVaultName) throws Exception {


        return  new ResponseEntity<>(azureSecretService.getSecret(request,keyVaultName), HttpStatus.OK);
    }

    @DeleteMapping("deleteKeyVaultSecretKey")
    public ResponseEntity<Map<String,String>> deleteSecret(HttpServletRequest request,@RequestParam String secretName,@RequestParam String keyVaultName) throws JSONException {

        return new ResponseEntity<Map<String,String>>(azureSecretService.deleteSecret(request,secretName,keyVaultName),HttpStatus.OK);
    }
}
