package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScaleAKSNodePool {
    private String resourceGroupName;
    private String aksName;
    private String nodePoolName;
    private int nodeCount;
}
