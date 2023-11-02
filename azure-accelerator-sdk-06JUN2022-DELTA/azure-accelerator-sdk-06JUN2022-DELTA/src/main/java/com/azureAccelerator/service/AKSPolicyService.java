package com.azureAccelerator.service;

import com.azureAccelerator.dto.AzureResourceDto;
import com.azureAccelerator.dto.InitiativesResponseDto;
import com.azureAccelerator.dto.PolicyParamDto;
import com.azureAccelerator.dto.PolicySetDto;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface AKSPolicyService {
    String createPolicy(HttpServletRequest request) throws IOException, JSONException;

    List<com.azureAccelerator.dto.AKSPolicyResponseDto> getPolicy(HttpServletRequest request) throws JSONException;

    com.azureAccelerator.dto.AKSPolicyAssignResponseDto assignPolicy(HttpServletRequest request,com.azureAccelerator.dto.AKSPolicyAssignRequestDto aksPolicyAssignRequestDto) throws JSONException;

    Map<String,String> assignPolicyInitiative(HttpServletRequest request,com.azureAccelerator.dto.InitiativeAssignRequestDto initiativeAssignRequestDto);


    PolicyParamDto getPolicyParamTypeWise(HttpServletRequest request,String policyName, String policyType) throws JSONException;

    List<AzureResourceDto> getResource(HttpServletRequest request, String resourceGroupName) throws JSONException;

    ArrayList<InitiativesResponseDto> getInitiativeVersions(HttpServletRequest request,String groupType) throws JSONException;

    ArrayList<InitiativesResponseDto> getListOfAssignPolicies(HttpServletRequest request) throws JSONException;

    Map<String,String> deleteInitiativePolicies(HttpServletRequest request,String initiativeID) throws JSONException;

    ArrayList<InitiativesResponseDto> getInitiativePolicies(HttpServletRequest request,String initiativeID) throws JSONException;




}