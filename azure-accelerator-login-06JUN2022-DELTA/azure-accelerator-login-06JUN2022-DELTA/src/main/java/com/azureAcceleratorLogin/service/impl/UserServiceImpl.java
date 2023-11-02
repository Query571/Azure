/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAcceleratorLogin.service.impl;

import com.azureAcceleratorLogin.dto.LocalUserDto;
import com.azureAcceleratorLogin.dto.UpdatePasswordDto;
import com.azureAcceleratorLogin.entity.User;
import com.azureAcceleratorLogin.exception.AzureAcltrRuntimeException;
import com.azureAcceleratorLogin.repository.UserRepository;
import com.azureAcceleratorLogin.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder encoder;
  private final ModelMapper modelMapper;

  @Autowired
  public UserServiceImpl(
          UserRepository userRepository,
          BCryptPasswordEncoder encoder,
          ModelMapper modelMapper) {
    this.userRepository = userRepository;
    this.encoder = encoder;
    this.modelMapper = modelMapper;
  }

  @Override
  public List<LocalUserDto> findAll() {
    return userRepository.findAll().stream()
        .map(entity -> modelMapper.map(entity, LocalUserDto.class))
        .collect(Collectors.toList());
  }

  @Override
  public LocalUserDto delete(long id) {
    User user = userRepository.findById(id).orElse(null);
    userRepository.deleteById(id);
    return modelMapper.map(user, LocalUserDto.class);
  }

  @Override
  public LocalUserDto addUser(LocalUserDto userDto) {
    User user = modelMapper.map(userDto, User.class);
    user.setPassWord("xoriant");
    user.setPassWord(encoder.encode(user.getPassWord()));
    user = userRepository.save(user);

    return modelMapper.map(user, LocalUserDto.class);
  }

  @Override
  public LocalUserDto updateUser(LocalUserDto userDto) {
    User user = modelMapper.map(userDto, User.class);
    User userFromDb = userRepository.findById(userDto.getId()).orElse(null);

    user.setPassWord(userFromDb.getPassWord());
    user = userRepository.save(user);

    return modelMapper.map(user, LocalUserDto.class);
  }

  @Override
  public List<LocalUserDto> findByName(String name) {
    return userRepository.findByFirstNameOrLastName(name, name).stream()
        .map(entity -> modelMapper.map(entity, LocalUserDto.class))
        .collect(Collectors.toList());
  }

  @Override
  public LocalUserDto updatePassword(UpdatePasswordDto userDto) {
    User user = userRepository.findById(userDto.getId()).orElse(null);
    if (encoder.matches(userDto.getOldPassword(), user.getPassWord())) {
      user.setPassWord(encoder.encode(userDto.getNewPassword()));
      user = userRepository.save(user);
      user.setPassWord(null);
    } else {
      throw new AzureAcltrRuntimeException(
          "Old Password doesn't match",
          null,
          "Old Password doesn't match",
          HttpStatus.FORBIDDEN);
    }

    return modelMapper.map(user, LocalUserDto.class);
  }

  @Override
  public void setInvalidCount(String userName) {
     userRepository.setInvalidCount(userName);
  }

  @Override
  public String getInvalidCount(String userName) {
    return userRepository.getInvalidCount(userName);
  }

  @Override
  public String getGenratedToken(String userName) {
    return userRepository.getGenratedToken(userName);
  }

  @Override
  public Optional<User> findByUsername(String userName) {

//    String []userList=userName.split("-");
//    System.out.println("userName------"+userName);
//    System.out.println("isrt------"+userList[0]);
//    Optional<User> user=userRepository.findByUserName(userList[0]);
//    if(userName.contains("SSO")){
//      if( user.get().getSsoStatus()!=null){
//        return user;
//      }else{
//        return null;
//      }
//    }else{
//      return user;
//    }
    return userRepository.findByUserOrEmail(userName);
    //return userRepository.findByUserName(userName);
  }

  @Override
  public void setInvalidCountToZero(String userName) {
    userRepository.setInvalidCountToZero(userName);
  }

  @Override
  public void generatedToken (String userName,String authToken) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, 93599);
    java.text.SimpleDateFormat sdf =
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String expireTime = sdf.format(calendar.getTime());
    userRepository.generatedToken(userName,authToken,expireTime);
  }

}
