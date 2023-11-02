/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
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
  private String contactNumber;
  private String department;
  private String email;
  private KeyValuDto role;
  private Timestamp createdAt;
  private String subscriptionId;

  @Override
  public String toString() {
    return "LocalUserDto{" +
            "id=" + id +
            ", userName='" + userName + '\'' +
            ", passWord='" + passWord + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", contactNumber='" + contactNumber + '\'' +
            ", department='" + department + '\'' +
            ", email='" + email + '\'' +
            ", role=" + role +
            ", createdAt=" + createdAt +
            ", subscriptionId=" + subscriptionId +
            '}';
  }
}
