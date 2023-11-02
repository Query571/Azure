package com.azureAccelerator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class ActionGroupDto {

  private String name;
  private String resourceGroup;
  private String receiver;
  private String emailId;
  private String countyCode;
  private String phoneNumber;
}

