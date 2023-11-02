package com.azureAccelerator.dto;

import com.microsoft.azure.management.network.VirtualNetworkPeeringState;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VNetPeeringResponseDto {
    private String id;
    private String networkId;
    private String name;
    private String location;
    private VirtualNetworkPeeringState state;
    private String remoteNetwokName;
    private boolean allowForwardedTraffic;
}
