package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class KeyVaultDto {
  private String name;
  private String resourceGroupName;
  private String location;
  private Map<String,String> tags;

}
