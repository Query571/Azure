package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InitiativeAssignResponseDto {

    private String displayName;
    private String description;
    private String policyDefinitionId;
    private String enforcementMode;
    private String locationForRemediation;
    private String identityType;

}
