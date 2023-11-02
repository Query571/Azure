package com.azureAccelerator.service;

import com.azure.core.credential.TokenRequestContext;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyType;

import com.azureAccelerator.dto.ImportKeyDto;
import com.azureAccelerator.dto.KeysDto;
import com.azureAccelerator.dto.KeysResponseDto;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface AzureKeysService {
    KeysResponseDto createKeys(HttpServletRequest request, KeysDto keysDto) throws Exception;
    Object getKeys(HttpServletRequest request,String keyVaultName) throws JSONException, Exception;
    Map<String,String> deleteKey(HttpServletRequest request,String keyName,String keyVaultName) throws JSONException;
    public String importKey(ImportKeyDto importKey) throws Exception;

}
