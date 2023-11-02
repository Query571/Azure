/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.controller;

import com.azureAccelerator.dto.SecretsDto;
import com.azureAccelerator.dto.SsoConfig;
import com.azureAccelerator.dto.StoredUserCredStatusDto;
import com.azureAccelerator.service.VaultService;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VaultController {

  private final VaultService vaultService;


  @Autowired
  public VaultController(
          VaultService vaultService) {
    this.vaultService = vaultService;
  }

  @PostMapping("/vault/newSecret")
  public ResponseEntity addSecret(
          @RequestBody SecretsDto secret,
          @RequestParam String secretName) {
    vaultService.addSecret(secret, secretName);
    return new ResponseEntity(HttpStatus.CREATED);
  }

  @DeleteMapping("/vault/removeSecret")
  public ResponseEntity<Map<String,String>> removeSecret(@RequestParam String secureId) {

    return new ResponseEntity<>(vaultService.removeSecret(secureId),HttpStatus.OK);
  }

  @PutMapping("/vault/updateSecret")
  public ResponseEntity<Map<String,String>> updateSecret(
          @RequestBody SecretsDto secret,
          @RequestParam String secretId) {

    return new ResponseEntity<>(vaultService.updateSecret(secret, secretId),HttpStatus.OK);
  }

  @GetMapping("/vault/StoredCredStatus")
  public ResponseEntity<Boolean> storedCredStatus() {
    return new ResponseEntity<>(
            vaultService.storedCredStatus(), HttpStatus.OK);
  }

  @GetMapping("/vault/StoredUserCredStatus")
  public ResponseEntity<StoredUserCredStatusDto> storedCredStatus(HttpServletRequest request) {
    String subscriptionId=request.getHeader("subscription_id");
    return new ResponseEntity<>(
            vaultService.storedUserCredStatus(request), HttpStatus.OK);
  }

  @GetMapping("/vault/cloudCredStoredStatus/{cloudProvider}")
  public ResponseEntity<Boolean> cloudCredStoredStatus(
          @PathVariable("cloudProvider") String cloudProvider) {
    return new ResponseEntity<>(
            vaultService.cloudCredStoredStatus(cloudProvider), HttpStatus.OK);
  }

  @GetMapping("/vault/getSecret")
  public ResponseEntity<SecretsDto> getSecret() {

    return new ResponseEntity<>(
            vaultService.getSecret(), HttpStatus.OK);
  }

  @GetMapping("/vault/getSecretList")
  public ResponseEntity<Object> getSecretList() {
    System.out.println("inside get secret list---->");

    return new ResponseEntity<>(
            vaultService.getSecretList(), HttpStatus.OK);
  }
  @GetMapping("/vault/getSSOClientIdStatus")
  public ResponseEntity<Object> getSSOClientIdStatus() throws Exception {
    return new ResponseEntity<>(
            vaultService.getSSOClientIdStatus(), HttpStatus.OK);
  }
  @PostMapping("/vault/addSSOInVault")
  public ResponseEntity addSSOInVault(
          @RequestParam String clientId,
          @RequestParam String tenteId,
          @RequestParam String redirectUrl) {
    vaultService.addSSOInVault(clientId, tenteId,redirectUrl);
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/vault/updateSSO")
  public ResponseEntity<Map<String,String>> updateSSO(
          @RequestParam String clientId,
          @RequestParam String tenteId,
          @RequestParam String redirectUrl) {

    return new ResponseEntity<>(vaultService.updateSSO(clientId, tenteId,redirectUrl),HttpStatus.OK);
  }

  @GetMapping("/getupdateSSO")
  public ResponseEntity<Map<String,String>> getupdateSSO(
          @RequestParam String clientId,
          @RequestParam String tenteId,
          @RequestParam String redirectUrl) {

    return new ResponseEntity<>(vaultService.updateSSO(clientId, tenteId,redirectUrl),HttpStatus.OK);
  }


  @GetMapping("/vault/getSecrets")
  public ResponseEntity< SecretsDto> getSecrets(@RequestParam String secretId) throws JSONException {

    return new ResponseEntity<>(vaultService.getSecrets(secretId), HttpStatus.OK);
  }
}
