package com.azureAccelerator.controller;

import com.azureAccelerator.dto.SSHKeyDto;
import com.azureAccelerator.service.SSHKeyClientService;
//import com.azureAccelerator.service.SSHKeyService;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;


@RestController
public class SshKeyController {

    private SSHKeyClientService sshKeyClientService;


    SshKeyController(SSHKeyClientService sshKeyClientService){
        this.sshKeyClientService=sshKeyClientService;
    }


    @PostMapping("createSshKey")
    public Map<String,String> createSSHKey(HttpServletRequest request, @RequestBody SSHKeyDto sshKeyDto) throws Exception {

        return sshKeyClientService.createSSHKey(request,sshKeyDto);
    }

    @GetMapping("getSshKey")
    public String getSshKey(HttpServletRequest request,@RequestParam String resourceGroupName,@RequestParam String sshPublicKeyName) throws Exception {

        return sshKeyClientService.getSSHKey(request,resourceGroupName,sshPublicKeyName);

    }
    @GetMapping("listingSshKeys")
    public JSONObject listSshKeys(HttpServletRequest request,@RequestParam String resourceGroups) throws Exception {

     return sshKeyClientService.listSshKeys(request,resourceGroups);
    }
}
