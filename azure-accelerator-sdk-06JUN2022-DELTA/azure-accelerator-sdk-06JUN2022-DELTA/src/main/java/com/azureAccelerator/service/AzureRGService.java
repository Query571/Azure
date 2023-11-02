package com.azureAccelerator.service;

import com.azureAccelerator.dto.AzureCommonDto;
import com.azureAccelerator.dto.AzureCommonResponseDto;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface AzureRGService {
    AzureCommonResponseDto createResourceGrp(HttpServletRequest request, String resourceGroupName, String rigonName) throws JSONException;
}
