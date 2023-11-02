/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAcceleratorLogin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VaultResponseDto {

  private String request_id;
  private String lease_id;
  private Boolean renewable;
  private Long lease_duration;
  private SecretsDto data;
  private String wrap_info;
  private String warnings;
  private String auth;
}
