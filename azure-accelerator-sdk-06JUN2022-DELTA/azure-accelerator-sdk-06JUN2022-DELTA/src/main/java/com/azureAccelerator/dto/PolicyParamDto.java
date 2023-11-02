package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PolicyParamDto {
    private String type;
    private List<?> paramList;
}
