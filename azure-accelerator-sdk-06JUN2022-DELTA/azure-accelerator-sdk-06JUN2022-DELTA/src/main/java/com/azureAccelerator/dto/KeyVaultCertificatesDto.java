package com.azureAccelerator.dto;

import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.keys.models.KeyType;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class KeyVaultCertificatesDto {


    private String keyVaultName;
    private String certificateName;
}
