package com.azureAccelerator.service;

import com.azureAccelerator.dto.KeyVaultDto;
import com.azureAccelerator.dto.KeyVaultResponseDto;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface AzureKeyVaultService {

  KeyVaultResponseDto createKeyVault(HttpServletRequest request,KeyVaultDto keyVaultDto) throws JSONException;

  String deleteKeyVault(HttpServletRequest request,String keyVaultDto) throws Exception;

  List<KeyVaultResponseDto> getKeyVault(HttpServletRequest request, String resourceGroupName) throws JSONException;
}
