/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.service;

import com.azureAccelerator.dto.SecretsDto;
import com.azureAccelerator.dto.SsoConfig;
import com.azureAccelerator.dto.StoredUserCredStatusDto;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


public interface VaultService {

  SecretsDto getSecrets(String vaultEndpoint, String vaultToken);

  void addSecret(SecretsDto secret, String secretName) ;

  Boolean storedCredStatus();

  StoredUserCredStatusDto storedUserCredStatus(HttpServletRequest request);

  Boolean cloudCredStoredStatus(String cloudProvider);

  SecretsDto getSecret();

  Object getSecretList();

  Object getSSOClientIdStatus() throws JSONException;

  Map<String,String> removeSecret(String secretName);

  Map<String,String> updateSecret(SecretsDto secret, String secretId);

  Map<String,String> updateSSO(String clientId,String  tenteId,String redirectUrl);

  void addSSOInVault(String clientId,String  tenteId,String redirectUrl);

  SecretsDto getSecrets(String subId) throws JSONException;
}
