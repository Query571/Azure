package com.azureAccelerator.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KeyProps {
    private boolean exportable;
    private String kty;
    private String key_size;
    private boolean reuse_key;
}
