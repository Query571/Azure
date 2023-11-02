package com.azureAccelerator.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class ActionGroupResponse {

  private String id;
  private String name;
  private String resourceGroup;
  //private String receiver;
  private List<String> emailId;
  private List<String> phoneNumber;
  //private String phoneNumber;
  private Map<String,String> tags;
}

