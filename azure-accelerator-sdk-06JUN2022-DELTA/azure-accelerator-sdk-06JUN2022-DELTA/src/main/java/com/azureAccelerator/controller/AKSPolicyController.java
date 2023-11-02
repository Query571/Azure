package com.azureAccelerator.controller;

import com.azureAccelerator.dto.*;
import com.azureAccelerator.service.AKSPolicyService;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class AKSPolicyController {

    private final AKSPolicyService aksPolicyService;

    public AKSPolicyController(AKSPolicyService aksPolicyService) {
        this.aksPolicyService = aksPolicyService;
    }

    @PostMapping("createPolicy")
    public ResponseEntity<String> createPolicy(HttpServletRequest request) throws IOException, JSONException {

        return new ResponseEntity<>(
                aksPolicyService.createPolicy(request), HttpStatus.OK);
    }

    @GetMapping("getPolicy")
    public ResponseEntity<List<AKSPolicyResponseDto>> getPolicy(HttpServletRequest request) throws JSONException {

        return new ResponseEntity<>(
                aksPolicyService.getPolicy(request), HttpStatus.OK);
    }


    @PostMapping("assignPolicy")
    public ResponseEntity<AKSPolicyAssignResponseDto> assignPolicy(HttpServletRequest request,@RequestBody AKSPolicyAssignRequestDto aksPolicyAssignRequestDto) throws JSONException {

        return new ResponseEntity<>(
                aksPolicyService.assignPolicy(request,aksPolicyAssignRequestDto), HttpStatus.OK);
    }

    @PostMapping("assignPolicyInitiative")
    public ResponseEntity<Map<String,String>> assignPolicyInitiative(HttpServletRequest request,@RequestBody InitiativeAssignRequestDto initiativeAssignRequestDto)  {

        return new ResponseEntity<>(
                aksPolicyService.assignPolicyInitiative(request,initiativeAssignRequestDto), HttpStatus.OK);
    }

    @GetMapping("getInitiativeVersions")
    public ResponseEntity<ArrayList<InitiativesResponseDto>> getInitiativeVersions(HttpServletRequest request,String groupType) throws JSONException {

        return new ResponseEntity<>(
                aksPolicyService.getInitiativeVersions(request,groupType), HttpStatus.OK);
    }

    @GetMapping("getInitiativePolicies")
    public ResponseEntity<ArrayList<InitiativesResponseDto>> getInitiativePolicies(HttpServletRequest request,String initiativeID) throws JSONException {

        return new ResponseEntity<>(
                aksPolicyService.getInitiativePolicies(request,initiativeID), HttpStatus.OK);
    }

    @GetMapping("getListOfAssignPolicies")
    public ResponseEntity<ArrayList<InitiativesResponseDto>> getListOfAssignPolicies(HttpServletRequest request) throws JSONException {

        return new ResponseEntity<>(
                aksPolicyService.getListOfAssignPolicies(request), HttpStatus.OK);
    }

    @DeleteMapping("deleteInitiative")
    public ResponseEntity<Map<String,String>> deleteInitiative(HttpServletRequest request,String initiativeID) throws JSONException {

        return new ResponseEntity<>(
                aksPolicyService.deleteInitiativePolicies(request,initiativeID), HttpStatus.OK);
    }



    @GetMapping("getPolicyParamTypeWise")
    public ResponseEntity<PolicyParamDto> getPolicyParamTypeWise(HttpServletRequest request,@RequestParam String policyName,@RequestParam String
            policyType) throws JSONException {

        return new ResponseEntity<>(
                aksPolicyService.getPolicyParamTypeWise(request,policyName,policyType), HttpStatus.OK);
    }

    @GetMapping("getResource")
    public ResponseEntity<List<AzureResourceDto>> getResource(HttpServletRequest request,
            @RequestParam String resourceGroupName) throws JSONException {

        return new ResponseEntity<>(
                aksPolicyService.getResource(request,resourceGroupName), HttpStatus.OK);
    }

}