package com.azureAcceleratorLogin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SecretsDto {

	private String username;
	private String password;
	private String url;
	private String subscriptionId ;
	private String clientId ;
	private String clientSecret ;
	private String tenantId;
    
}
