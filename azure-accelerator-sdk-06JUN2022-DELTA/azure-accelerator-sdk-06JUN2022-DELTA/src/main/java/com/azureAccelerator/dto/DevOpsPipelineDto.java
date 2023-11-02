package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DevOpsPipelineDto {
    private String token;
    private String organization;
    private String projectName;
    private String pipelineId;
    private String runId;
    private String logId;
}
