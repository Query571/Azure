/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAcceleratorLogin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocalUserDto {

  private long id;
  private String userName;
  private String passWord;
  private String firstName;
  private String lastName;
  private Timestamp createdAt;
}
