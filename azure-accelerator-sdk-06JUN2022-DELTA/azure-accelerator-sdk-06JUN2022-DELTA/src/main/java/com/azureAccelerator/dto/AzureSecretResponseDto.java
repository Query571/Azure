package com.azureAccelerator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class AzureSecretResponseDto {
    private String id;
    //private String name;
    //private String secretURI;
    private String secretKey;
    private String secretValue;
}
