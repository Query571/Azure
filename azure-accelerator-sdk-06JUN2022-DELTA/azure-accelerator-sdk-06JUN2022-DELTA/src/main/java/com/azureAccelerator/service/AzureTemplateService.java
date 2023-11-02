package com.azureAccelerator.service;

import com.azureAccelerator.dto.AzureCommonResponseDto;
import com.azureAccelerator.dto.ExportTempDto;
import org.json.JSONException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface AzureTemplateService {

  AzureCommonResponseDto exportTemplate(HttpServletRequest request, ExportTempDto exportTempDto) throws JSONException;

  String exportAllTemplates(HttpServletRequest request,String resourceGroupName) throws JSONException;

  AzureCommonResponseDto uploadTemplate(HttpServletRequest request,MultipartFile uploadTemplateDto, String resourceGroup) throws JSONException;
}
