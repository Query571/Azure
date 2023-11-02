/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAcceleratorLogin.util;

import com.azureAcceleratorLogin.dto.SecretsDto;
import com.azureAcceleratorLogin.dto.VaultResponseDto;
import com.azureAcceleratorLogin.exception.AzureAcltrRuntimeException;
import com.azureAcceleratorLogin.exception.RecordNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class VaultUtil {

  private static String param = "X-Vault-Token";

  public SecretsDto getSecrets(String vaultEndpoint, String vaultToken) {
    try {
      HttpHeaders requestHeaders = new HttpHeaders();
      requestHeaders.set(param, vaultToken);
      requestHeaders.setContentType(MediaType.APPLICATION_JSON);
      return new RestTemplate()
          .exchange(
              vaultEndpoint,
              HttpMethod.GET,
              new HttpEntity<Object>("parameters", requestHeaders),
              VaultResponseDto.class)
          .getBody()
          .getData();
      } catch (Throwable t) {
      throw new RecordNotFoundException("Error reading vault secret,Please enter valid credentials", t);
    }
  }

  public static void addSecrets(String vaultEndpoint, String vaultToken, SecretsDto secret) {
    try {
      HttpHeaders requestHeaders = new HttpHeaders();
      requestHeaders.set(param, vaultToken);
      requestHeaders.setContentType(MediaType.APPLICATION_JSON);
      new RestTemplate().postForObject(
          vaultEndpoint,
          new HttpEntity<Object>(
              new ObjectMapper().writeValueAsString(secret),
              requestHeaders),
          HttpEntity.class);
    } catch (Throwable t) {
      throw new AzureAcltrRuntimeException(
          t.getMessage(),
          null,
          "Error adding vault secret",
          HttpStatus.INTERNAL_SERVER_ERROR,
          t);
    }
  }
}
