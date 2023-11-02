package com.azureAccelerator.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class AzureCommonResponseDto {
     private String name;
     private Map<String,String> tags;
}
