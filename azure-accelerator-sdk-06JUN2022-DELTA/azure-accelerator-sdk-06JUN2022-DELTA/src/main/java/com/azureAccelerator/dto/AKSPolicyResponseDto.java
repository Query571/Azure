package com.azureAccelerator.dto;

import com.microsoft.azure.management.resources.PolicyType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class AKSPolicyResponseDto {
    private String id;
    private String name;
    private String definitionLocation;
    private String type;
    private String definitionType;
    private String description;
    private String filterGroup;

    @Override
    public String toString() {
        return "AKSPolicyResponseDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", definitionLocation='" + definitionLocation + '\'' +
                ", type='" + type + '\'' +
                ", definitionType='" + definitionType + '\'' +
                ", description='" + description + '\'' +
                ", filterGroup='" + filterGroup + '\'' +
                '}';
    }
}
