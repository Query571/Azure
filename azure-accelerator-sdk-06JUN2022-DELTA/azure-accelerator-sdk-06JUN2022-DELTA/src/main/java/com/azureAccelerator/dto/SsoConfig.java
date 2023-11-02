package com.azureAccelerator.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SsoConfig {
    private String redirectUrl;
    private String clientId ;
    private String tenantId;
    private boolean ssoStatus;
    private SsoConfig data;
}
