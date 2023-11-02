package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AKSPolicyAssignRequestDto {
    private String id;
    private String name;
    private String description;
    private String policyType;
    private List<String> excludedScopes;
    private List<String> parameters;
    private String parameterType;
    private String resourceGroupName;

    @Override
    public String toString() {
        return "AKSPolicyAssignRequestDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", policyType='" + policyType + '\'' +
                ", excludedScopes=" + excludedScopes +
                ", parameters=" + parameters +
                ", parameterType='" + parameterType + '\'' +
                ", resourceGroupName='" + resourceGroupName + '\'' +
                '}';
    }
}