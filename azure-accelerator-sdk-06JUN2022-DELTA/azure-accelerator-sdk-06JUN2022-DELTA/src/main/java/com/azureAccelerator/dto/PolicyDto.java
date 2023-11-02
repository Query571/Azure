package com.azureAccelerator.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PolicyDto {
    private KeyProps key_props;
    private SecretProps secret_props;
    private X509Props x509_props;
    private Issuer issuer;
    private String certificateName;
    private String keyvaultName;
    private String issuer2;
    private String subject;
}
