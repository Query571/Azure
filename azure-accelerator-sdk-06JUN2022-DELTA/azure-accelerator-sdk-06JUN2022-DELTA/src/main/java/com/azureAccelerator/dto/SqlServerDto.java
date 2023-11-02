package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SqlServerDto {
    private String location;
    private String resourceGroupName;
    private String serverName;
    private String adminUser;
    private String adminPassword;

}
