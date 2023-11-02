/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.util;

import com.azureAccelerator.dto.SsoConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.azureAccelerator.dto.SecretsDto;
import com.azureAccelerator.dto.VaultResponseDto;
import com.azureAccelerator.dto.VaultResponseListDto;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.exception.RecordNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	public static void addSecretsSSO(String vaultEndpoint, String vaultToken, String clientId,String  tenteId,String redirectUrl) {
	  SsoConfig sso=new SsoConfig();
		sso.setClientId(clientId);
		sso.setTenantId(tenteId);
		sso.setRedirectUrl(redirectUrl);

		try {
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.set(param, vaultToken);
			requestHeaders.setContentType(MediaType.APPLICATION_JSON);
			new RestTemplate().postForObject(
					vaultEndpoint,
					new HttpEntity<Object>(
							new ObjectMapper().writeValueAsString(sso),
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

	public static Object getSecretsList(String vaultEndpoint, String vaultToken) {
		try {
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.set(param, vaultToken);
			requestHeaders.setContentType(MediaType.APPLICATION_JSON);
			Object secretList = new RestTemplate().exchange(vaultEndpoint, HttpMethod.GET,
					new HttpEntity<Object>("parameters", requestHeaders), VaultResponseListDto.class).getBody()
					.getData();

			JSONObject jsonObjList = new JSONObject(secretList.toString());

			return jsonObjList.toString();

		} catch (Throwable t) {
			//throw new RecordNotFoundException("Error reading vault secret,Please enter valid credentials", t);
			return null;
		}
	}

	public static void addSecretsList(String vaultEndpoint, String vaultToken, SecretsDto secret) {
		try {

			Object secretList = getSecretsList(vaultEndpoint, vaultToken);

			JSONObject newSecret = new JSONObject();
			newSecret.put("secretId", secret.getSubscriptionId());
			newSecret.put("secretDetails", new ObjectMapper().writeValueAsString(secret));
			newSecret.put("secretActiveRole", secret.getRolePermission());

			if (secretList != null) {

				JSONObject jsonObj = new JSONObject(secretList.toString());
				jsonObj.getJSONArray("secret").put(newSecret);
				storeVaultCredentials(vaultEndpoint, vaultToken, jsonObj.toString());

			} else {

				JSONObject jsonObj = new JSONObject();
				jsonObj.put("secret", new JSONArray());
				jsonObj.getJSONArray("secret").put(newSecret);
				storeVaultCredentials(vaultEndpoint, vaultToken, jsonObj.toString());

			}

		} catch (Throwable t) {
			throw new AzureAcltrRuntimeException(t.getMessage(), null, "Error adding vault secret",
					HttpStatus.INTERNAL_SERVER_ERROR, t);
		}
	}

	public static void storeVaultCredentials(String vaultEndpoint,String vaultToken,String jsonString) {
		
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set(param, vaultToken);
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		
		new RestTemplate().postForObject(vaultEndpoint, new HttpEntity<Object>(jsonString, requestHeaders),
				HttpEntity.class);
		
	}
}
