package com.azureAccelerator.dto;

import com.azure.resourcemanager.containerregistry.models.Sku;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Setter
@Getter
@Builder
public class
AKSContainerRegistryResponseDto {
    private String name;
    private String loginServerUrl;
    private String location;
    private String creationDate;
    private Map<String,String> tags;
}
