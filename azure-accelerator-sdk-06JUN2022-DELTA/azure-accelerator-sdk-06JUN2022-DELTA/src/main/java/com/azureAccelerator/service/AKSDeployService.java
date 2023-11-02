package com.azureAccelerator.service;

import com.azureAccelerator.dto.*;
import com.azureAccelerator.entity.AzureInstanceTypes;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface AKSDeployService {

  AKSDeployResponse deployAKS(HttpServletRequest request,AKSDeployDto aksDeployDto) throws Exception;

  List<AKSDeployResponse> getAKSClusters(HttpServletRequest request, String resourceGroupName) throws JSONException;

  List<AzureInstanceTypes> getAzureVMSizes(HttpServletRequest request);

  void deleteAKS(HttpServletRequest request,String aksId) throws Exception;

  String getRGLocation(HttpServletRequest request,String resourceGroupName) throws JSONException;

  AzureCommonResponseDto deployAppOnAKS(HttpServletRequest request,String resourceGroupName, String aksName, MultipartFile file) throws JSONException;

  AzureCommonResponseDto addNodePool(HttpServletRequest request,AddNodePoolDto addNodePoolDto) throws JSONException;

  List<String> getKubernetVersion(HttpServletRequest request,String resourceGroupName, String aksName) throws JSONException;

  AzureCommonResponseDto scaleNode(HttpServletRequest request,ScaleAKSNodePool scaleAKSNodePool) throws JSONException;

  AzureCommonResponseDto upgradeKubernetVersion(HttpServletRequest request,UpgrKuberNetVerDto upgrKuberNetVerDto) throws JSONException;

  AKSDashboardDto getKbrntDashboard(HttpServletRequest request,String resourceGroupName, String aksName) throws JSONException;

  Map<String,String> aksStart(HttpServletRequest request,String resourceGroupName, String aksName) throws JSONException, IOException;

  Map<String,String> aksStop(HttpServletRequest request,String resourceGroupName, String aksName) throws JSONException, IOException;
}
