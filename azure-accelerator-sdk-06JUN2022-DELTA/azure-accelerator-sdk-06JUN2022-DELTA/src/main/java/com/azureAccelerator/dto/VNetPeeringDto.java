package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VNetPeeringDto {
    private String resourceGroup;
    private String sourceVNetId;
    private String remoteVNetId;
    private String peeringName;
}
