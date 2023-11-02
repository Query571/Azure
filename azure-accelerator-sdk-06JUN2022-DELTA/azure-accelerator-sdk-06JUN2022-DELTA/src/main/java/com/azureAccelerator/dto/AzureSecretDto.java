package com.azureAccelerator.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@Builder
public class AzureSecretDto {
        private String vaultName;
    private String secretKey;
    private String secretValue;
    private Map<String,String> tags;
}
