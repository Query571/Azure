package com.azureAccelerator.controller;

import com.azureAccelerator.service.ARMDefaultTemplateService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ARMDefaultTemplateController {

    private final ARMDefaultTemplateService armDefaultTemplateService;

    public ARMDefaultTemplateController(ARMDefaultTemplateService armDefaultTemplateService) {
        this.armDefaultTemplateService = armDefaultTemplateService;
    }


    @GetMapping("defaultARMTemplate")
    public ResponseEntity<Object> defaultARMTemplate(HttpServletRequest request, @RequestParam String templateName, @RequestParam int count) throws Exception {

        return new ResponseEntity<Object>(armDefaultTemplateService.defaultJsonTypeARM(request,templateName,count), HttpStatus.OK);
    }
}
