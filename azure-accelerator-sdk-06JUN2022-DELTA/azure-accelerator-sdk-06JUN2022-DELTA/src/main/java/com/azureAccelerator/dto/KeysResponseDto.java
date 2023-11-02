package com.azureAccelerator.dto;

import com.azure.security.keyvault.keys.models.KeyType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class KeysResponseDto {
    private String keyId;
    private String keyName;
    private KeyType keyType;
}
