package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SqlDBDto {

    private String sqlDBName;
    private String resourceGroupName;
    private String sqlServerName;
    private boolean configurable;
}
