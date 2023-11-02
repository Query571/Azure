package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class InitiativeAssignRequestDto {

    private String displayName;
    private String description;
    private String policyDefinitionId;
    private String enforcementMode;
    private String locationForRemediation;
    private String identityType;
    private String resourceGroup;
    private List<String> excludedScopes;

    @Override
    public String toString() {
        return "InitiativeAssignRequestDto{" +
                "displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", policyDefinitionId='" + policyDefinitionId + '\'' +
                ", enforcementMode='" + enforcementMode + '\'' +
                ", locationForRemediation='" + locationForRemediation + '\'' +
                ", identityType='" + identityType + '\'' +
                ", resourceGroup='" + resourceGroup + '\'' +
                ", excludedScopes=" + excludedScopes +
                '}';
    }
}
