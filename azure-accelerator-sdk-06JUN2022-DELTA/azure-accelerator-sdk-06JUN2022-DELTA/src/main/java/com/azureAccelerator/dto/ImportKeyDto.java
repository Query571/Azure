package com.azureAccelerator.dto;

import com.azure.security.keyvault.keys.models.JsonWebKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.File;

@Setter
@Getter
@NoArgsConstructor
public class ImportKeyDto {
    private String keyVault;
    private String keyName;
    private JsonWebKey jsonWebKey;
    private File file;
    private PemReader pemReader;
}
