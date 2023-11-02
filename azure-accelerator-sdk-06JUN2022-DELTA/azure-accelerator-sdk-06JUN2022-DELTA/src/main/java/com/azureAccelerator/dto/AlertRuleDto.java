package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AlertRuleDto {

  private String targetResourceName;
  private String resourceGroup;
  private String resourceId;
  private String actionGroupId;

}
