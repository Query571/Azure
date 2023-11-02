package com.azureAccelerator.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExportTempDto {

  private String resourceGroupName;
  private List<String> resourceId;
  private String resourceName;

}
