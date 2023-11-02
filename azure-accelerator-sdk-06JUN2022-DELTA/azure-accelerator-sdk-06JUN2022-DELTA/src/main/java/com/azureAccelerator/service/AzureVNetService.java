package com.azureAccelerator.service;

import com.azureAccelerator.dto.*;
import com.microsoft.azure.management.network.Network;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public interface AzureVNetService {

  VNetResponseDto createVNet(HttpServletRequest request, VNetDto vNetDto) throws Exception;

  Network updateVNet(HttpServletRequest request) throws JSONException;

  String deleteVNet(HttpServletRequest request,String vNetId) throws Exception;

  public String deleteVNets(HttpServletRequest request,List<VNetDto> vNetDto) throws Exception;

  List<String> resourceGroups(HttpServletRequest request) throws JSONException;

  List<VNetResponseDto> getVnets(HttpServletRequest request,String resourceGroupName,String region) throws JSONException;

  VNetPeeringResponseDto createVNetPeering(HttpServletRequest request,VNetPeeringDto vNetPeeringDto) throws JSONException;

  String deleteVNetPeering(HttpServletRequest request,String sourceVNetId, String peeringId) throws JSONException;

  List<VNetPeeringResponseDto> getVnetPeering(HttpServletRequest request,String sourceVNetId) throws JSONException;

  VNetPeeringResponseDto updateVnetPeering(HttpServletRequest request,UpdateVnetPeeringDto updateVnetPeeringDto) throws JSONException;

  List<String> getLocations(HttpServletRequest request) throws JSONException;

  List<VNetResponseDto> getVNetsByRegion(HttpServletRequest request,String resourceGroupName,String region) throws JSONException;

  VNetResponseDto getSubnets(HttpServletRequest request,String vNetName, String resourceGroupName,String activeFlag,String nsgName) throws JSONException;
}
