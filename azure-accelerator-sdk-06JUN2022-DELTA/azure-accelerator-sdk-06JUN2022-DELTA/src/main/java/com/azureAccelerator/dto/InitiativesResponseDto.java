package com.azureAccelerator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
@Builder
public class InitiativesResponseDto {

    private String displayName;
    private String policyType;
    private String policyDescription;
    private String name;
    private String id;
    private String policyDefinitionId;
    private String policyScope;
    private ArrayList <String> resourceGroup;
    private boolean assignStatus;




}
