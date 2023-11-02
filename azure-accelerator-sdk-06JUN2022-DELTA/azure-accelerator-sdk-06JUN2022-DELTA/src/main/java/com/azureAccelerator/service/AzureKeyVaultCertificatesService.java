package com.azureAccelerator.service;

import com.azureAccelerator.dto.KeyVaultCertificatesDto;
import com.azureAccelerator.dto.KeyVaultCertificatesResponseDto;
import com.azureAccelerator.dto.PolicyDto;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface AzureKeyVaultCertificatesService {

    KeyVaultCertificatesResponseDto createCertificate(HttpServletRequest request, KeyVaultCertificatesDto keyVaultCertificatesDto);

    List<KeyVaultCertificatesResponseDto> listCertificate(HttpServletRequest request,String keyVaultCertificatesDto) throws JSONException;

    Map<String,String> deleteCertificate(HttpServletRequest request,String keyVaultName,String certificateName) throws JSONException;

    String createCertificate2(HttpServletRequest request,PolicyDto policyDto);

}
