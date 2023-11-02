package com.azureAccelerator.controller;

import com.azureAccelerator.dto.*;
import com.azureAccelerator.service.AzureKeyVaultCertificatesService;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


@RestController
public class AzureKeyVaultCertificatesController {

    private AzureKeyVaultCertificatesService azureKeyVaultCertificatesService;

    public AzureKeyVaultCertificatesController(AzureKeyVaultCertificatesService azureKeyVaultCertificatesService) {
        this.azureKeyVaultCertificatesService = azureKeyVaultCertificatesService;
    }


    @PostMapping("createKeyVaultCertificate")
    ResponseEntity<KeyVaultCertificatesResponseDto> createSecretKey(HttpServletRequest request, @RequestBody KeyVaultCertificatesDto keyVaultCertificatesDto){

        return new ResponseEntity<KeyVaultCertificatesResponseDto>(azureKeyVaultCertificatesService.createCertificate(request,keyVaultCertificatesDto),HttpStatus.CREATED);

    }
    @GetMapping("listAllCertificates")
    ResponseEntity<List<KeyVaultCertificatesResponseDto>> listCertificate(HttpServletRequest request,@RequestParam String keyVaultName) throws JSONException {

        return new ResponseEntity<List<KeyVaultCertificatesResponseDto>>(azureKeyVaultCertificatesService.listCertificate(request,keyVaultName),HttpStatus.OK);

    }

    @DeleteMapping("deleteCertificate")
    ResponseEntity<Map<String,String>> deleteCertificate(HttpServletRequest request,@RequestParam String keyVaultName,String certificateName) throws JSONException {

        return new ResponseEntity<Map<String,String>>(azureKeyVaultCertificatesService.deleteCertificate(request,keyVaultName,certificateName),HttpStatus.OK);

    }
    @PostMapping("createKeyVaultCertificate2")
    ResponseEntity<String> createSecretKey(HttpServletRequest request,@RequestBody PolicyDto policyDto){

        return new ResponseEntity<String>(azureKeyVaultCertificatesService.createCertificate2(request,policyDto),HttpStatus.CREATED);

    }

}
