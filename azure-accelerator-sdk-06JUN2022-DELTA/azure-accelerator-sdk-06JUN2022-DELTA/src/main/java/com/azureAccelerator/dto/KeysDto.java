package com.azureAccelerator.dto;


import com.azure.security.keyvault.keys.models.KeyType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeysDto {
    private String kId;
    private String keyVaultName;
    private String keyName;
    private KeyType keyType;
}
