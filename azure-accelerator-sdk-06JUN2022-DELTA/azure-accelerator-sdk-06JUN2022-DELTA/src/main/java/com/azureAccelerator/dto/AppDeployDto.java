package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppDeployDto {

  private String resourceGroupName;
  private String aksName;
  private String deployFileName;

}
