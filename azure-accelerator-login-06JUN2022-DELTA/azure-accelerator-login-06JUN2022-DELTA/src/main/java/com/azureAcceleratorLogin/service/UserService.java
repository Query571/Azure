/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAcceleratorLogin.service;

import com.azureAcceleratorLogin.dto.LocalUserDto;
import com.azureAcceleratorLogin.dto.UpdatePasswordDto;
import com.azureAcceleratorLogin.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

  LocalUserDto addUser(LocalUserDto user);

  LocalUserDto updateUser(LocalUserDto user);

  List<LocalUserDto> findAll();

  LocalUserDto delete(long id);

  List<LocalUserDto> findByName(String name);

  LocalUserDto updatePassword(UpdatePasswordDto user);


  void setInvalidCount(String username);

  void setInvalidCountToZero(String username);

  void generatedToken(String username,String authToken);

  String getInvalidCount(String username);

  String getGenratedToken(String username);

  Optional<User> findByUsername(String username);
}
