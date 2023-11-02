package com.azureAccelerator.service;

import com.azureAccelerator.dto.NetworkSecurityGroupsDto;
import com.azureAccelerator.dto.NetworkSecurityGroupsResponseDto;
import org.json.JSONException;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface NetworkSecurityGroupsService {


    String createNSG(HttpServletRequest request, NetworkSecurityGroupsDto networkSecurityGroupsDto) throws Exception;

    JSONObject listAllNSG(HttpServletRequest request,String resourceGroupName) throws IOException, JSONException;

    String getNSG(HttpServletRequest request,String resourceGroupName, String networkSecurityGroupName) throws IOException, JSONException;

    JSONObject getNSG1(HttpServletRequest request,String resourceGroupName, String networkSecurityGroupName) throws IOException, JSONException;

    Map<String,String> deleteNSG(HttpServletRequest request,String resourceGroupName, String networkSecurityGroupName) throws IOException, JSONException;

    Map<String,String> updateSubnet(HttpServletRequest request,String resourceGroupName, String vNetName, String subnetName, String nsg) throws JSONException;

    Map<String,String> disAssociationSubnet(HttpServletRequest request,String resourceGroupName,String vNetName,String subnetName) throws JSONException;

    String getSubNet(HttpServletRequest request,String resourceGroupName,String vNetName,String subnetName) throws IOException, JSONException;

    NetworkSecurityGroupsResponseDto addNSGRule(HttpServletRequest request,NetworkSecurityGroupsDto networkSecurityGroupsDto) throws Exception;

    Map<String,String> removeRule(HttpServletRequest request,String resourceGroupName,String myNsg,String ruleName) throws Exception;

    NetworkSecurityGroupsResponseDto updateNsgRule(HttpServletRequest request,NetworkSecurityGroupsDto networkSecurityGroupsDto) throws Exception;

    List<Map<String,String>> getAssociatedSubnets(HttpServletRequest request,String resourceGroupName,String nsg) throws JSONException;
}
