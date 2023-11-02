package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateVnetPeeringDto {
    private  String sourceVNetId;
    private String peeringId;
    private boolean allowForwardedTraffic;

}
