package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddNodePoolDto {

    private String aksName;
    private String nodePoolName;
    private String resourceGroupName;
    private int nodeCount;
    private String systemType;
    private String nodeSize;
    private int maxPods;
    private int nodeDiskSize;
}
