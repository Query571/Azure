package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class TokenDto {

    private String grantType;
    private String clientSecret;
    private String resource;
}
