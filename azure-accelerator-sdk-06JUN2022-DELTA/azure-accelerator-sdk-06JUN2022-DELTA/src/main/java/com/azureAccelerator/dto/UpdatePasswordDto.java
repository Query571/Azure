/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordDto {

  private Long id;
  private String userUID;
  private String oldPassword;
  private String newPassword;

  @Override
  public String toString() {
    return "UpdatePasswordDto{" +
            "id=" + id +
            ", userUUID='" + userUID + '\'' +
            ", oldPassword='" + oldPassword + '\'' +
            ", newPassword='" + newPassword + '\'' +
            '}';
  }
}
