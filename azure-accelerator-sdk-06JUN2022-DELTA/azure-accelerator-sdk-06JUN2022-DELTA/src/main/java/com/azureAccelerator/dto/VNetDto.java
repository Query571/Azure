package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class VNetDto {

  private String name;
  private String region;
  private String resourceGroupName;
  private Properties2 properties;
  private String addressSpace;
  /*private String subnetName;
  private String subnetAddressRange;*/
  private List<SubnetDto> subnetDtoList;
  private Map<String,String> tags;
}
