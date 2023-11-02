package com.azureAccelerator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AppRegistrationResponse {
    private String id;
    private String name;
    private String appId;
    private String clientId;
    private String clientSecret;
    private String message;
}
