package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class StroageDto {

    private String location;
    private String storageName;
    private String resourceGroupName;
    private String storageKind;
    private Map<String,String> tags;
}

