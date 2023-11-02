package com.azureAccelerator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SqlDBResponseDto {
    private String id;
    private String name;
    private String location;
    private String resourceGroupName;

}
