package com.azureAccelerator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AKSDashboardDto {

    private String dashboardIp;
    private String token;
}
