package com.azureAccelerator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class VNetResponseDto {

  private String id;
  private String name;
  private String location;
  private String addressSpace;
  private SubnetDto subnetDto;
  private boolean isVNetPeered;
  private List<SubnetDto> subnetDtoList;
  private Map<String,String> tags;

}
