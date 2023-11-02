package com.azureAccelerator.dto;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AzureCredentials {

  private ApplicationTokenCredentials applicationTokenCredentials;
  private String subscriptionId;
}
