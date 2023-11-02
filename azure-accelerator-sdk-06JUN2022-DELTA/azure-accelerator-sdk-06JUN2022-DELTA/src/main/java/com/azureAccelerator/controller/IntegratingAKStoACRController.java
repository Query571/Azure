package com.azureAccelerator.controller;


import com.azureAccelerator.service.IntegratingAKStoACRService;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class IntegratingAKStoACRController {

    private IntegratingAKStoACRService integratingAKStoACRService;

    public IntegratingAKStoACRController(IntegratingAKStoACRService integratingAKStoACRService) {
        this.integratingAKStoACRService = integratingAKStoACRService;
    }
    @PutMapping("integrating")
    public ResponseEntity<String> integration(HttpServletRequest request, @RequestParam String resourceGroupName, @RequestParam String aksName, @RequestParam String acr) throws JSONException {

        return new ResponseEntity<String>(integratingAKStoACRService.integratingAKStoACR(request,resourceGroupName,aksName,acr), HttpStatus.OK);
    }

    @PutMapping("disIntegrating")
    public ResponseEntity<String> disIntegration(HttpServletRequest request,@RequestParam String resourceGroupName, @RequestParam String aksName,@RequestParam String acr) {

        return new ResponseEntity<String>(integratingAKStoACRService.disIntegratingAKStoACR(request,resourceGroupName,aksName,acr),HttpStatus.OK);
    }

    @PostMapping("importingDockerImages")
    public ResponseEntity<Map<String,String>> importingDockerImages(HttpServletRequest request,@RequestParam String acrName, @RequestParam String image, @RequestParam String version) throws JSONException {

        return new ResponseEntity<Map<String,String>>(integratingAKStoACRService.importingDockerImages(request,acrName,image,version), HttpStatus.OK);
    }

    @PostMapping("importingPrivateDockerHubImages")
    public ResponseEntity<Map<String,String>> importingPrivateDockerHubImages(HttpServletRequest request,@RequestParam String acrName, @RequestParam String image, @RequestParam String version,@RequestParam String userName,@RequestParam String password) throws JSONException {

        return new ResponseEntity<Map<String,String>>(integratingAKStoACRService.importingPrivateDockerhubImages(request,acrName,image,version,userName,password), HttpStatus.OK);
    }

    @PostMapping("importingDockerImagesAcrToAcr")
    public ResponseEntity<Map<String,String>> importingDockerImagesACRToACR(HttpServletRequest request,@RequestParam String acrResourceName,@RequestParam String destinationAcrName,@RequestParam String imageName,@RequestParam String version) throws JSONException {

        return new ResponseEntity<Map<String,String>>(integratingAKStoACRService.importingDockerImagesACRToACR(request,acrResourceName,destinationAcrName,imageName,version), HttpStatus.OK);
    }

    @DeleteMapping("deleteDockerImagesInAcr")
    public ResponseEntity<String> deleteDockerImagesInAcr(HttpServletRequest request,@RequestParam String acrName,@RequestParam String imageName) throws JSONException {

        return new ResponseEntity<String>(integratingAKStoACRService.deleteDockerImagesInAcr(request,acrName,imageName),HttpStatus.OK);
    }
    @PostMapping("checkAcrNameAvailability")
    public ResponseEntity<JSONObject> checkNameAvailability(HttpServletRequest request,@RequestParam String acrName) throws JSONException {

        return new ResponseEntity<JSONObject>(integratingAKStoACRService.checkNameAvailability(request,acrName),HttpStatus.OK);
    }

    @PutMapping("enableKeystoreForAks")
    public ResponseEntity<Map<String,String>> enableKeystoreForAks(HttpServletRequest request,@RequestParam String resourceGroupName, @RequestParam String aksName) throws JSONException {

        return new ResponseEntity<Map<String,String>>(integratingAKStoACRService.enableKeystoreForAks(request,resourceGroupName,aksName),HttpStatus.OK);
    }

    @PutMapping("disableKeystoreForAks")
    public ResponseEntity<Map<String,String>> disableKeystoreForAks(HttpServletRequest request,@RequestParam String resourceGroupName,@RequestParam String aksName) throws JSONException {

        return new ResponseEntity<Map<String,String>>(integratingAKStoACRService.disableKeystoreForAks(request,resourceGroupName,aksName),HttpStatus.OK);
    }
    @GetMapping("CheckingAzureKeyVaultSecretsProvider")
    public ResponseEntity<JSONObject> CheckingAzureKeyVaultSecretsProvider(HttpServletRequest request,@RequestParam String resourceGroupName,@RequestParam String aksName) throws JSONException {

        return new ResponseEntity<JSONObject>(integratingAKStoACRService.CheckingAzureKeyVaultSecretsProvider(request,resourceGroupName,aksName),HttpStatus.OK);
    }

    @GetMapping("getImageTags")
    public ResponseEntity<Object> getTags(HttpServletRequest request,@RequestParam String acrName,@RequestParam String image) throws JSONException {

        return new ResponseEntity<Object>(integratingAKStoACRService.getImageTag(request,acrName,image),HttpStatus.OK);

    }
    @PutMapping("aksDashboardDeploy")
    public ResponseEntity<String> dashboardDeploy(HttpServletRequest request,String rg, String aksName) throws JSONException {
        return new ResponseEntity<String>(integratingAKStoACRService.dashboardDeploy(request,rg,aksName),HttpStatus.OK);

    }
    @GetMapping("getImagetagsList")
    public ResponseEntity<List<String>> listTags(HttpServletRequest request, String image) throws Exception {
        return new ResponseEntity<>(integratingAKStoACRService.listTags(request,image),HttpStatus.OK);
    }
}
