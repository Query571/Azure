package com.azureAccelerator.dto;

import com.azure.resourcemanager.resources.models.ParameterValuesValue;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class AKSPolicyAssignResponseDto {
    private String id;
    private String name;
    private String scope;
    private String description;
    private List<String> excludedScopes;
    private Map<String, ParameterValuesValue> parameters;


}
