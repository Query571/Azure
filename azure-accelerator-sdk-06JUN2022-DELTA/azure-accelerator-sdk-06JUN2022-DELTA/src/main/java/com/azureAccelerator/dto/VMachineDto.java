package com.azureAccelerator.dto;


import com.azure.core.management.Region;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.NetworkInterface;
import java.util.Map;

@Setter
@Getter
@ToString
public class VMachineDto {

    private String name;
    private String region;
    private String resourceGroupName;
    private String vnet;
    private String subnet;
    private String userName;
    private String password;
    private String image;
    private String imageFlavour;
    private String sshKey;
    private String computerName;
    private String size;
    private Map<String,String> tags;

}
