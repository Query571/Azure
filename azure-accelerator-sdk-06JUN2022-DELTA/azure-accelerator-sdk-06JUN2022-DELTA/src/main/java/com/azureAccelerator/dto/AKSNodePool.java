package com.azureAccelerator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AKSNodePool {

  private String name;
  private String vmSize;
  private int vmCount;
  private String osType;
  private String poolMode;
  private String agentPoolType;
  private String provisioningState;
  private String powerState;
  private String version;
}
