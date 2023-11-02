package com.azureAccelerator.dto;

import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class WebAppDto {

  private String webAppId;
  private String webAppName;
  private String location;
  private String status;
  private String appServicePlanId;
  private String appServicePlanName;
  private Set<String> hostName;
  private String operatingSystem;
  private Map<String,String> tags;

}
