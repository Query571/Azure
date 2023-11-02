package com.azureAccelerator.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SSHKeyDto {

    //private String location;
    private String resourceGroupName;
    private String sshPublicKeyName;
}
