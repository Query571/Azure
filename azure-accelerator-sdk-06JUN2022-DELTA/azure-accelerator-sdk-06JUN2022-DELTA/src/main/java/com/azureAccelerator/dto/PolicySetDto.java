package com.azureAccelerator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@Builder
public class PolicySetDto {
    List<PolicyDefinitionDto> policyDefinitionId;
    String id;
    String name;
    String policySetDisplayName;
    String policyType;
    String policySetDescription;


}