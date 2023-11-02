package com.azureAccelerator.controller;

import com.azureAccelerator.dto.AppRegistrationResponse;
import com.azureAccelerator.service.AzureAppRegistrationService;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class AzureAppRegistrationController {

    private final AzureAppRegistrationService azureAppRegistrationService;

    public AzureAppRegistrationController(AzureAppRegistrationService azureAppRegistrationService) {
        this.azureAppRegistrationService = azureAppRegistrationService;
    }

    @PostMapping("createAppReg")
    public ResponseEntity<AppRegistrationResponse> createAppReg(HttpServletRequest request, @RequestParam String appName,@RequestParam String appUri) throws JSONException {

        return new ResponseEntity<>(
                azureAppRegistrationService.createAppReg(request,appName,appUri), HttpStatus.CREATED);
    }

    @GetMapping("getAppReg")
    public ResponseEntity<List<AppRegistrationResponse>> getAppReg(HttpServletRequest request) throws JSONException {

        return new ResponseEntity<>(
                azureAppRegistrationService.getAppReg(request), HttpStatus.OK);
    }

    @GetMapping("deleteAppReg")
    public ResponseEntity<String> deleteAppReg(HttpServletRequest request,String appRegId) throws JSONException {

        return new ResponseEntity<>(
                azureAppRegistrationService.deleteAppReg(request,appRegId), HttpStatus.OK);
    }
}
