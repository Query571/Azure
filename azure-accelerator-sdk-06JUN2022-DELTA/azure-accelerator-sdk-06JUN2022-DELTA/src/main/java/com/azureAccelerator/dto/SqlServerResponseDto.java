package com.azureAccelerator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@Builder
public class SqlServerResponseDto {
    private String id;
    private String name;
    private String location;
    private String resourceGroupName;
    private Map<String,String> tags;
}
