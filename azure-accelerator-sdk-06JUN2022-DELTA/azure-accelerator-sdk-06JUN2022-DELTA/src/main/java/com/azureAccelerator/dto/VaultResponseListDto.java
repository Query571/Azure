/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VaultResponseListDto {

  private String request_id;
  private String lease_id;
  private Boolean renewable;
  private Long lease_duration;
  private Object data;
  private String wrap_info;
  private String warnings;
  private String auth;
}
