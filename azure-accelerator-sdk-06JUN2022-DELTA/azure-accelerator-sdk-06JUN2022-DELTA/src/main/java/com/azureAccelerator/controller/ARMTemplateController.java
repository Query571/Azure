package com.azureAccelerator.controller;

import com.azureAccelerator.service.ARMTemplateService;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ARMTemplateController {

    private ARMTemplateService armTemplateService;

    public ARMTemplateController(ARMTemplateService armTemplateService) {
        this.armTemplateService = armTemplateService;
    }


    @GetMapping("getVnetARMTemplate")
    public ResponseEntity<Object> getVnetARMTemplate(HttpServletRequest request, @RequestParam String resourceGroup, @RequestParam String vNet) throws JSONException {

        return  new ResponseEntity<Object>(armTemplateService.getVnetARMTemplate(request,resourceGroup,vNet), HttpStatus.OK);
    }

    @GetMapping("getKeyVaultARMTemplate")
    public ResponseEntity<Object> getKeyVaultARMTemplate(HttpServletRequest request,@RequestParam String resourceGroup,@RequestParam String keyVault) throws JSONException {


        return  new ResponseEntity<Object>(armTemplateService.getKeyVaultARMTemplate(request,resourceGroup,keyVault), HttpStatus.OK);
    }

    @GetMapping("getVMARMTemplate")
    public ResponseEntity<Object> getVMARMTemplate(HttpServletRequest request,@RequestParam String resourceGroup,@RequestParam String vm) throws JSONException {


        return  new ResponseEntity<Object>(armTemplateService.getVMARMTemplate(request,resourceGroup,vm), HttpStatus.OK);
    }

    @GetMapping("getNSGARMTemplate")
    public ResponseEntity<Object> getNSGARMTemplate(HttpServletRequest request,@RequestParam String resourceGroup,@RequestParam String nsg) throws JSONException {


        return  new ResponseEntity<Object>(armTemplateService.getNSGARMTemplate(request,resourceGroup,nsg), HttpStatus.OK);
    }

    @GetMapping("getACRARMTemplate")
    public ResponseEntity<Object> getACRARMTemplate(HttpServletRequest request,@RequestParam String resourceGroup,@RequestParam String acr) throws JSONException {


        return  new ResponseEntity<Object>(armTemplateService.getACRARMTemplate(request,resourceGroup,acr), HttpStatus.OK);
    }
}
