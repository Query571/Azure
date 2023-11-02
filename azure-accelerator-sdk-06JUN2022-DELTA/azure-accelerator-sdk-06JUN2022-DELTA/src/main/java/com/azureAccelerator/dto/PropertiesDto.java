package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
public class PropertiesDto {
    private String protocol;
    private String sourceAddressPrefix;
    private String destinationAddressPrefix;
    private String access;
    private String destinationPortRange;
    private String sourcePortRange;
    private int priority;
    private String direction;
}
