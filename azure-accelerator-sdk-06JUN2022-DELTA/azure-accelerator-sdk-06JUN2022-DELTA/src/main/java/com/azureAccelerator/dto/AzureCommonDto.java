package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class AzureCommonDto {
    private String resourceGroupName;
    private String rigonName;
    private Map<String,String> tags;
}
