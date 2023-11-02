/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "username")
  private String userName;

  @Column(name = "password")
  private String passWord;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "contact_number")
  private String contactNumber;

  @Column(name = "department")
  private String department;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "email")
  private String email;

  @Column(name = "user_uid", updatable = false)
  private String userUID;

  @Column(name = "disable")
  private Boolean disabled;

  @Column(name = "secret_key")
  private String secret;

  @Column(name = "failed_attempts")
  private Integer failedAttempts;


  @CreationTimestamp
  @Column(name = "created_at",updatable = false)
  private Timestamp createdAt;


  @OneToOne(fetch = FetchType.EAGER)
  @JoinTable(
          name = "users_roles",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Role roles;

  @Column(name = "subscription_id")
  private String subscriptionId;

  public void addRole(Role role) {
    this.roles=role;
  }

  @Override
  public String toString() {
    return "User{" +
            "id=" + id +
            ", userName='" + userName + '\'' +
            ", passWord='" + passWord + '\'' +
            ", firstName='" + firstName + '\'' +
            ", contactNumber='" + contactNumber + '\'' +
            ", department='" + department + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", userUID='" + userUID + '\'' +
            ", createdAt=" + createdAt +
            ", roles=" + roles +
            ", subscriptionId=" + subscriptionId +
            '}';
  }
}
