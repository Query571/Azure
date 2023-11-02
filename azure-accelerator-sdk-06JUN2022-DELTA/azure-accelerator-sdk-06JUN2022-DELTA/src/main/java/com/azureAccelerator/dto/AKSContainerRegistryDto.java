package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class AKSContainerRegistryDto {
    private String name;
    private String resourceGroupName;
    private Map<String,String> tags;
}
