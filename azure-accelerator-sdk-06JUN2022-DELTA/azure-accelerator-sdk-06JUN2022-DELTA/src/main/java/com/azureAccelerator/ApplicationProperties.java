/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ApplicationProperties {

 /* @Value("${server.servlet.context-path}")
  private String servletContextPath;

  @Value("${spring.user.search.base}")
  private String userSearchBase;

  @Value("${spring.user.search.filter}")
  private String userSearchFilter;

  @Value("${security.oauth2.client.client-id}")
  private String clientId;

  @Value("${security.oauth2.client.client-secret}")
  private String clientSecret;

  @Value("${security.jwt.resource-ids}")
  private String resourceIds;

  @Value("${security.signing-key}")
  private String signingKey;*/

  @Value("${spring.datasource.driver-class-name}")
  private String driverClass;

 /* @Value("${spring.group.search.base}")
  private String groupSearchBase;

  @Value("${spring.group.search.filter}")
  private String groupSearchFilter;*/

  @Value("${vault.token}")
  private String vaultToken;

  @Value("${vault.endpoint}")
  private String vaultEndpoint;

  @Value("${vault.azure.secret}")
  private String vaultAzureSecret;

   @Value("${vault.mysql.secret}")
  private String vaultMysqlSecret;

  @Value("${app.script.path}")
  private String appScriptPath;

  @Value("${app.deploy.path}")
  private String appDeployPath;
  
  @Value("${vault.azure.secretList}")
  private String vaultAzureSecretList;

  @Value("${vault.sso.ssosecret}")
  private String ssoSecret;

}
