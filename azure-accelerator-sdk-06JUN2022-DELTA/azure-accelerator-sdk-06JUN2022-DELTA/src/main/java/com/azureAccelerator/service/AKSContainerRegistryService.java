package com.azureAccelerator.service;

import com.azureAccelerator.dto.AKSContainerRegistryResponseDto;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface AKSContainerRegistryService {
    List<AKSContainerRegistryResponseDto> getContainerRegistry(HttpServletRequest request,String resourceGroupName) throws JSONException;

    AKSContainerRegistryResponseDto createConainerReg(HttpServletRequest request,String name, String resourceGroupName,String region,String acrSize) throws JSONException;

    List<String> getContainerRepository(HttpServletRequest request,String containerRegName) throws JSONException;

    Map<String,String> deleteAcr(HttpServletRequest request, String resourceGroupName, String registryName) throws Exception;


}
