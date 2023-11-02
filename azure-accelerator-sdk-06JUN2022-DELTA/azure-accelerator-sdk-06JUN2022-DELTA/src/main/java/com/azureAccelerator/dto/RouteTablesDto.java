package com.azureAccelerator.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RouteTablesDto {

    private String resourceGroupName;
    private String routeTableName;
    private String region;
}
