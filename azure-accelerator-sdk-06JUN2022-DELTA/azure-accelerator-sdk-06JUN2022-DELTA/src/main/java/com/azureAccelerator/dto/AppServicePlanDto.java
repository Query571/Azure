package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Setter
@Getter
public class AppServicePlanDto {

  private String Name;
  private String resourceGroupName;

}
