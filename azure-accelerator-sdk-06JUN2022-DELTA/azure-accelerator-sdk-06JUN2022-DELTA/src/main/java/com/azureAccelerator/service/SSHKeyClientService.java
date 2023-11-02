package com.azureAccelerator.service;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.SSHKeyDto;
import com.azureAccelerator.dto.TokenDto;
import org.json.JSONException;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.Map;

public interface SSHKeyClientService {


    Map<String,String> createSSHKey(HttpServletRequest request, SSHKeyDto sshKeyDto) throws Exception;
    String getSSHKey(HttpServletRequest request,String resourceGroupName,String sshPublicKeyName) throws Exception;
    //public String getSSHKeyFromVm(String resourceGroupName,String sshPublicKeyName,VaultService vaultService, ApplicationProperties applicationProperties ) throws IOException, JSONException ;
    JSONObject listSshKeys(HttpServletRequest request,String resourceGroups) throws Exception;

}
