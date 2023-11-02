package com.azureAccelerator.dto;

import com.microsoft.azure.management.containerservice.ManagedClusterAddonProfile;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class AKSDeployDto {

  private String aksName;
  private String location;
  private String resourceGroup;
  private String rootUserName;
  private String sshKey;
  private String vmSize;
  private int vmCount;
  private String agentPoolMode;
  private String systemType;
  private int diskSizeInGB;
  private int maxPodsCount;
  private Map<String, ManagedClusterAddonProfile> addOnProfileMap;
  private String acrName;

}
