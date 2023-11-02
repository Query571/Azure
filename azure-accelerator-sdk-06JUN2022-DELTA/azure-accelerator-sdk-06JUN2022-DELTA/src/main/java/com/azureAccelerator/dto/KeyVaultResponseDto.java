package com.azureAccelerator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@Builder
public class KeyVaultResponseDto {

  private String id;
  private String name;
  private String vaultURI;
  private String location;
  private Map<String,String> tags;
}
