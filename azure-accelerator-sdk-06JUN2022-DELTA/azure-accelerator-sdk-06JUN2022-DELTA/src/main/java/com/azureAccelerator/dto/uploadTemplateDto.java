package com.azureAccelerator.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.File;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class uploadTemplateDto {

  private String resourceGroup;
  private File file;

}
