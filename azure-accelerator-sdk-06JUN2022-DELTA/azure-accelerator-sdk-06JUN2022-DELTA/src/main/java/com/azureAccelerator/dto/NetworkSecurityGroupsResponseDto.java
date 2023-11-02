package com.azureAccelerator.dto;

import com.azure.resourcemanager.network.models.NetworkSecurityRule;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@Builder
public class NetworkSecurityGroupsResponseDto {

    private String name;
    private String id;
    private String region;
    private String resourceGroupName;
    private String outBound;
    private String inBound;
    private int port;
    private String address;
    private String protocol;
    private String range;
    private int priority;
    private String description;
    private Map<String,String> tags;
    private Map<String, NetworkSecurityRule> rules;
}
