package com.azureAccelerator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Builder
@Setter
@Getter
public class RouteTablesResponseDto {
    private String id;
    private String name;
    private String type;
    private String region;
    private String properties;
    private Map<String,String> tags;
}
