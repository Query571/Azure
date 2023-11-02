/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.service;

import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.dto.UpdatePasswordDto;
import com.azureAccelerator.dto.UserRoleDto;
import com.azureAccelerator.entity.User;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {

  LocalUserDto addUser(LocalUserDto user);

  LocalUserDto updateUser(LocalUserDto user);

  Map<String, String> findAll();

  List<UserRoleDto> getAllRole();

  LocalUserDto delete(long id);


  List<LocalUserDto> findByName(String name);

  List<LocalUserDto> findByEmail(String email);

  public Map<String,String> updateSubIdByUser(String  userName,String subId);

  LocalUserDto updatePassword(UpdatePasswordDto user);

  Optional<User> findByUsername(String username);

  Map<Integer,String> unlockUser(String uniqueUserId);

  Map<Integer,String> lockUser(String uniqueUserId);

  Map<String, String> userLoginStatus(String username, String jwtToken);

  Map<Integer,String> userLogout(String username);

  Map<Integer,String> getPublicKey(String username) throws Exception;

  Map<String,String> getSecretKey(String username);

  Map<String,String> deleteSecretKey(String username);

  Map<String,String> setSecretKey(String username, String key);

  Map<String,String> setSSOtoken(String username, String key);

  Map<String,String> updatedSSOConfiguration(String clientID,String oldCleintID,String filePath) throws IOException;

}
