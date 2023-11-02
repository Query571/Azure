package com.azureAccelerator.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.management.network.SecurityRuleProtocol;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.beans.factory.annotation.Value;


import java.util.List;
import java.util.Map;

@Setter
@Getter
@ToString
public class NetworkSecurityGroupsDto {

    //private int n;
    private String name;
    private String location;
    private String resourceGroupName;
   // @Value("private")
   // private String nsgType;
    //private String outBound;
    private String bound;
    //private String inBound;
    private boolean permission;
    //private int port;
    private String sourceRange;
    private String destinationRange;
    private String fromAddress;
    private String toAddress;
    //private String protocol;
    //private String range;
   /* private int sourceToRange;
    private int sourceFromRange;*/
    private int priority;
    //private String description;
    private Map<String,String> tags;
    //@JsonProperty(value = "")
    private PropertiesDto1 properties;
    private String ruleName;
    private SecurityRuleProtocol protocol;
    /*private int destinationToRange;
    private int destinationFromRange;*/

}
