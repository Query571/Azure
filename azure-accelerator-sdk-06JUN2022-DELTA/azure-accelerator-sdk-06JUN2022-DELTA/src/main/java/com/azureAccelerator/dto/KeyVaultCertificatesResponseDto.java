package com.azureAccelerator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class KeyVaultCertificatesResponseDto {
    private String CertificateId;
    private String CertificateName;
}
