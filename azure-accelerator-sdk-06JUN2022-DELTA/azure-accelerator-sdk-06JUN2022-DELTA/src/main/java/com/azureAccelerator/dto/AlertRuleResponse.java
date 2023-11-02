package com.azureAccelerator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@Builder
public class AlertRuleResponse {

  private String alertName;
  private String description;
  private String condition;
  private String targetResource;
  private String actionGroupName;
  private boolean enabled;
  private Map<String,String> tags;
}
