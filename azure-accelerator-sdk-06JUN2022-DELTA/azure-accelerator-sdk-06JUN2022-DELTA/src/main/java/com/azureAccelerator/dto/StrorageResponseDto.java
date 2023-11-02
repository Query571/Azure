package com.azureAccelerator.dto;

import com.microsoft.azure.management.storage.Kind;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@Builder
public class StrorageResponseDto {
    private String id;
    private String name;
    private String location;
    private Kind storageKind;
    private Map<String,String> tags;
}
