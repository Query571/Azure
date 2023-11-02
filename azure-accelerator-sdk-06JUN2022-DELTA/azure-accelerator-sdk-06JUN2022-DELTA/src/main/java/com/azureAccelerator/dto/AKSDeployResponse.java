package com.azureAccelerator.dto;

import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.containerservice.ManagedClusterAddonProfile;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class AKSDeployResponse {

  private String aksId;
  private String aksName;
  private String kubernetesVersion;
  private String resourceGroup;
  private String status;
  private String location;
  private Map<String, ManagedClusterAddonProfile> isEnabled;
  private String powerState;
 /* private String networkType;
  private String dnsServiceIP;
  private String podCidr;
  private String serviceCidr;
  private String dockerBridgeCidr;*/
  private List<AKSNodePool> aksNodePool;


}
