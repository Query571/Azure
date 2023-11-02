/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.service.impl;

import com.azureAccelerator.dto.LocalUserDto;
import com.azureAccelerator.dto.UpdatePasswordDto;
import com.azureAccelerator.dto.UserRoleDto;
import com.azureAccelerator.entity.Role;
import com.azureAccelerator.entity.User;
import com.azureAccelerator.exception.AzureAcltrRuntimeException;
import com.azureAccelerator.repository.RoleRepository;
import com.azureAccelerator.repository.UserRepository;
import com.azureAccelerator.service.UserService;
import com.azureAccelerator.util.RSA_Read_Write_Key;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

  private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final BCryptPasswordEncoder encoder;
  private final ModelMapper modelMapper;

  @Autowired
  public UserServiceImpl(
          UserRepository userRepository,
          RoleRepository roleRepository,
          BCryptPasswordEncoder encoder,
          ModelMapper modelMapper) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.encoder = encoder;
    this.modelMapper = modelMapper;
  }

  @Override
  public Map<String, String> findAll() {
    List<String> response = new ArrayList<>();
    logger.info("find All");
    userRepository.findAll().forEach(user -> {
      try {
        user.setDisabled(user.getFailedAttempts()>=6?true:false);

        response.add( new ObjectMapper().writer().writeValueAsString(user));
        logger.debug(response);
      } catch (JsonProcessingException e) {
        logger.error("Error occurred while converting string::::::"+e.getMessage());
      }

    });
    Base64.Encoder basEncoder = Base64.getEncoder();
    Map<String,String> mapResponse=new HashMap<>();
    mapResponse.put("Status","200");
    mapResponse.put("response",basEncoder.encodeToString(String.valueOf(response.toString()).getBytes()));
    logger.debug(mapResponse);
    return mapResponse;
  }

  @Override
  public List<UserRoleDto> getAllRole() {
    logger.info("getting all roles...");
    return roleRepository.findAll().stream()
            .map(entity -> modelMapper.map(entity, UserRoleDto.class))
            .collect(Collectors.toList());
  }

  @Override
  public LocalUserDto delete(long id) {
    logger.info("deleting user");
    User user = userRepository.findById(id).orElse(null);
    logger.debug("Id :"+id);
    userRepository.deleteById(id);
    logger.info("deleted");
    return modelMapper.map(user, LocalUserDto.class);
  }

  @Override
  public LocalUserDto addUser(LocalUserDto userDto) {
    logger.info("adding the user...");
    UUID uuid=UUID.randomUUID();
    User user = modelMapper.map(userDto, User.class);
    user.setUserUID(String.valueOf(uuid));
    user.setDisabled(false);
    user.setFailedAttempts(0);
    Base64.Decoder decoder = Base64.getDecoder();
    String newPass = new String(decoder.decode(user.getPassWord()));
    user.setPassWord(encoder.encode(newPass));
    Role roleUser = roleRepository.findByName(userDto.getRole().getName());
    user.addRole(roleUser);
    user = userRepository.save(user);
    logger.info("adding the user ended");
    return modelMapper.map(user, LocalUserDto.class);
  }






  @Override
  public List<LocalUserDto> findByName(String name) {

  logger.info("findByName...");
    return userRepository.findByUserName(name).stream()
            .map(entity -> modelMapper.map(entity, LocalUserDto.class))
            .collect(Collectors.toList());

  }
  @Override
  public List<LocalUserDto> findByEmail(String email) {
    logger.info("findByEmail"+email);
    return userRepository.findByEmail(email).stream()
            .map(entity -> modelMapper.map(entity, LocalUserDto.class))
            .collect(Collectors.toList());
  }

  @Override
  public Map<String,String>  updateSubIdByUser(String  userName,String subId) {

    logger.info("updateSubIdByUser started...");
    Map<String,String> map=new HashMap<>();
    userRepository.updateSubID(userName,subId);
    map.put("status","Subscription Id : "+subId +" is Successfully updated");
    logger.debug(map);
    logger.info("updateSubIdByUser ended");

    return map;

  }

  @Override
  public LocalUserDto updateUser(LocalUserDto userDto) {
    logger.info("updateUser started...");
    User user = modelMapper.map(userDto, User.class);
    User userFromDb = userRepository.findById(userDto.getId()).orElse(null);
    Base64.Decoder decoder = Base64.getDecoder();
    String decPass = new String(decoder.decode(userDto.getPassWord()));
    userDto.setPassWord(decPass);
    if(!userFromDb.getPassWord().equals(decPass)){
      user.setPassWord(encoder.encode(userDto.getPassWord()));
      userRepository.setInvalidCount(user.getUserName());
    }else{
      user.setPassWord(userFromDb.getPassWord());
    }
    Role roleUser = roleRepository.findByName(userDto.getRole().getName());
    user.addRole(roleUser);
    user.setDisabled(false);
    user.setFailedAttempts(0);
    user = userRepository.save(user);
    logger.info("updateUser ended");
    return modelMapper.map(user, LocalUserDto.class);
  }



  @Override
  public LocalUserDto updatePassword(UpdatePasswordDto userDto) {
    logger.info("updatePassword started...");
    Base64.Decoder decoder = Base64.getDecoder();
    // Decoding string
    String newPass = new String(decoder.decode(userDto.getNewPassword()));
    String oldPass = new String(decoder.decode(userDto.getOldPassword()));
    userDto.setOldPassword(String.valueOf(decoder.decode(userDto.getOldPassword())));
    userDto.setNewPassword(String.valueOf(decoder.decode(userDto.getNewPassword())));

    User user = userRepository.findByUserUID(userDto.getUserUID()).orElse(null);
    if(userDto.getOldPassword().equals(userDto.getNewPassword())){
      throw new AzureAcltrRuntimeException(
              "Old Password and New Password can not be same",
              null,
              "Old Password and New Password can not be same",
              HttpStatus.FORBIDDEN);
    }
    else if (encoder.matches(oldPass, user.getPassWord())) {
      user.setPassWord(encoder.encode(newPass));
      user = userRepository.save(user);
      user.setPassWord(null);
    } else {
      throw new AzureAcltrRuntimeException(
          "Old Password doesn't match",
          null,
          "Old Password doesn't match",
          HttpStatus.FORBIDDEN);
    }
    logger.info("password Changed");

    return modelMapper.map(user, LocalUserDto.class);

//    return null;
  }

  @Override
  public Optional<User> findByUsername(String userName) {
    return null;
  }

  @Override
  public Map<Integer, String> unlockUser(String uniqueUserId) {
    logger.info("unlockUser started...");
    User user = userRepository.findByUserUID(uniqueUserId).orElse(null);
    HashMap<Integer,String> map=new HashMap<>();

    if(user!=null) {
      user.setDisabled(false);
      user.setFailedAttempts(0);
      userRepository.save(user);
      map.put(200,"User unlock successfully");
      logger.debug(map);
    }else {
      map.put(400,"Fail");
      logger.debug(map);
    }
    logger.info(map);
    logger.info("unlockUser ended...");
    return map;
  }

  @Override
  public Map<Integer, String> lockUser(String uniqueUserId) {
    User user = userRepository.findByUserUID(uniqueUserId).orElse(null);
    HashMap<Integer,String> map=new HashMap<>();
    if(user!=null){
      user.setDisabled(true);
      user.setFailedAttempts(6);
      userRepository.save(user);
      map.put(200,"User lock successfully");

    }else{
      map.put(400,"Fail");
    }
    logger.info(map);
    return map;
  }


  @Override
  public Map<String, String> userLoginStatus(String name, String jwtToken) {

    Base64.Decoder decoder = Base64.getDecoder();
    name= new String(decoder.decode(name));
    HashMap<String,String> loginStatusResp=new HashMap<>();
    User user=userRepository.tokenExpiry(name);
    String status=userRepository.userLoginStatus(name);
    if( (status!=null && !status.equals("1")) || user==null){
      userRepository.generatedToken(name);
      loginStatusResp.put("Status","200");
      loginStatusResp.put("response","User is not logged in");
    }else{
      loginStatusResp.put("Status","401");
      loginStatusResp.put("response","Logged User");
    }
    logger.info(loginStatusResp);
    return loginStatusResp;

  }

  @Override
  public Map<Integer,String> userLogout(String name) {
    Base64.Decoder decoder = Base64.getDecoder();
    name= new String(decoder.decode(name));
    HashMap<Integer,String> loginStatusResp=new HashMap<>();
    userRepository.userLogout(name);
    loginStatusResp.put(200,"Success");
    logger.info(loginStatusResp);
    return loginStatusResp;

  }

  @Override
  public Map<Integer,String> getPublicKey(String plainText) throws Exception {
    HashMap<Integer,String> loginStatusResp=new HashMap<>();
    loginStatusResp.put(200,"Success");
    // genrateKey()
    RSA_Read_Write_Key.genrateKey();
    logger.info("plainText Text : " + plainText);

    String encryptText=RSA_Read_Write_Key.getEncrypt(plainText);
    logger.info("encryptText Text : " + encryptText);

    String decryptText=RSA_Read_Write_Key.getDecrypt(encryptText);

    logger.info("decryptText Text : " + decryptText);

    return loginStatusResp;
  }
  @Override
  public Map<String,String> setSecretKey(String username, String key)  {
    HashMap<String,String> loginStatusResp=new HashMap<>();
    loginStatusResp.put("200","Success");
    Base64.Decoder decoder = Base64.getDecoder();
    username= new String(decoder.decode(username));
    key= new String(decoder.decode(key));
    userRepository.setSecretKey(username,key);
    logger.info(loginStatusResp);
    return loginStatusResp;
  }

  @Override
  public Map<String,String> getSecretKey(String username)  {
    HashMap<String,String> loginStatusResp=new HashMap<>();
    loginStatusResp.put("200","Success");
    Base64.Decoder decoder = Base64.getDecoder();
    username= new String(decoder.decode(username));
    loginStatusResp.put("key",encoder.encode(userRepository.getSecretKey(username)));
    logger.info(loginStatusResp);
    return loginStatusResp;
  }

  @Override
  public Map<String,String> deleteSecretKey(String username)  {
    HashMap<String,String> loginStatusResp=new HashMap<>();
    loginStatusResp.put("200","Success");
    Base64.Decoder decoder = Base64.getDecoder();
    username= new String(decoder.decode(username));
    userRepository.deleteSecretKey(username);
    logger.info(loginStatusResp);
    return loginStatusResp;
  }


  @Override
  public Map<String,String> setSSOtoken(String username, String key)  {
    HashMap<String,String> loginStatusResp=new HashMap<>();
    loginStatusResp.put("200","Success");
    Base64.Decoder decoder = Base64.getDecoder();
    username= new String(decoder.decode(username));
    key= new String(decoder.decode(key));
    userRepository.setSSOToken(username,key);
    logger.info(loginStatusResp);
    return loginStatusResp;
  }

  @Override
  public Map<String,String> updatedSSOConfiguration(String clientID,String oldCleintID,String filePath) throws IOException {
    HashMap<String,String> loginStatusResp=new HashMap<>();
    loginStatusResp.put("200","Success");
    final String dir = System.getProperty("user.dir");
    System.out.println("current dir = " + dir);
    loginStatusResp.put("path",dir);

    if(filePath==null){
         filePath = "opt/config/configuration.json";
      }
System.out.println("filePAth--->"+filePath);
    //Instantiating the Scanner class to read the file
    Scanner sc = new Scanner(new File(filePath));
    //instantiating the StringBuffer class
    StringBuffer buffer = new StringBuffer();
    //Reading lines of the file and appending them to StringBuffer
    while (sc.hasNextLine()) {
      buffer.append(sc.nextLine()+System.lineSeparator());
    }
    String fileContents = buffer.toString();
    System.out.println("Contents of the file: "+fileContents);
    //closing the Scanner object
    sc.close();
    String oldClientID = oldCleintID;
    String newClientID= clientID;
    //Replacing the old line with new line
    fileContents = fileContents.replaceAll(oldClientID, newClientID);
    //instantiating the FileWriter class
    FileWriter writer = new FileWriter(filePath);
    System.out.println("");
    System.out.println("new data: "+fileContents);
    writer.append(fileContents);
    writer.flush();
    return loginStatusResp;
  }

}