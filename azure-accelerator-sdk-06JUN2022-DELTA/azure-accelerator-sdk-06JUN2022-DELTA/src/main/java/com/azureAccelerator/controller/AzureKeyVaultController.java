package com.azureAccelerator.controller;

import com.azureAccelerator.dto.KeyVaultDto;
import com.azureAccelerator.dto.KeyVaultResponseDto;
import com.azureAccelerator.dto.VNetDto;
import com.azureAccelerator.dto.VNetResponseDto;
import com.azureAccelerator.service.AzureKeyVaultService;
import java.util.List;

import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AzureKeyVaultController {

  private final AzureKeyVaultService azureKeyVaultService;

  public AzureKeyVaultController(
      AzureKeyVaultService azureKeyVaultService) {
    this.azureKeyVaultService = azureKeyVaultService;
  }


  @GetMapping("getKeyVault")
  public ResponseEntity<List<KeyVaultResponseDto>> getKeyVault(HttpServletRequest request,
      @RequestParam String resourceGroupName) throws JSONException {

    return new ResponseEntity<List<KeyVaultResponseDto>>(
        azureKeyVaultService.getKeyVault(request,resourceGroupName), HttpStatus.OK);
  }

  @PostMapping("createKeyVault")
  public ResponseEntity<KeyVaultResponseDto> createKeyVault(HttpServletRequest request,
      @RequestBody KeyVaultDto keyVaultDto) throws JSONException {

    return new ResponseEntity<KeyVaultResponseDto>(
        azureKeyVaultService.createKeyVault(request,keyVaultDto), HttpStatus.CREATED);
  }

  @DeleteMapping("deleteKeyVault")
  public ResponseEntity<String> deleteKeyVault(HttpServletRequest request,@RequestParam String  keyVaultId) throws Exception {

    return new ResponseEntity<String>(
        azureKeyVaultService.deleteKeyVault(request,keyVaultId), HttpStatus.OK);
  }

}
