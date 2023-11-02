package com.azureAccelerator.service;

import com.azureAccelerator.dto.StoragesDto;
import com.azureAccelerator.dto.StroageDto;
import com.azureAccelerator.dto.StrorageResponseDto;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface AzureStrorageService {
    StrorageResponseDto createStorage(HttpServletRequest request, StroageDto stroageDto) throws JSONException;

    //StrorageResponseDto createStorage2(StroageDto stroageDto);

    List<StrorageResponseDto> storage(HttpServletRequest request,String resourceGroupName) throws JSONException;

    Map<String,String> deleteStorage(HttpServletRequest request,String storageId) throws Exception;

    public Map<String,String> deleteStorages(HttpServletRequest request, List<StoragesDto> storageIds)throws JSONException;

}
