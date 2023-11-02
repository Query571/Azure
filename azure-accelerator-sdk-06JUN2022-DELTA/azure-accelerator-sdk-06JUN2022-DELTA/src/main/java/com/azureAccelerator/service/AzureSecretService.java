package com.azureAccelerator.service;

//import com.azureAccelerator.dto.AzureScretResponseDto;
import com.azureAccelerator.dto.AzureSecretDto;
import com.azureAccelerator.dto.AzureSecretResponseDto;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface AzureSecretService {
    AzureSecretResponseDto createSecretKey(HttpServletRequest request, AzureSecretDto secretKeyDto) throws Exception;
    Object getSecret(HttpServletRequest request,String keyVaultName) throws JSONException, Exception;
    Map<String,String> deleteSecret(HttpServletRequest request,String secretName,String keyVaultName) throws JSONException;
}
