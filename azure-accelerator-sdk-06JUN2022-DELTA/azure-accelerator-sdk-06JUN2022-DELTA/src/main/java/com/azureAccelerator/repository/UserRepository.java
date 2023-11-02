/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.repository;


import com.azureAccelerator.entity.Role;
import com.azureAccelerator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUserName(String userName);

  List<User> findByFirstNameOrLastName(String firstName, String lastName);

  Optional<User> findByEmail(String email);

  Optional<User> findByUserUID(String userUID);

  List<User> findByRolesIn(Collection<String> names);
  List<User> findByRoles_(Role role);

  @Modifying
  @Transactional
  @Query(value = "UPDATE users SET failed_attempts =0 where username= ?1", nativeQuery = true)
  void setInvalidCount(String username);

  @Modifying
  @Transactional
  @Query(value = "UPDATE users SET  subscription_id =?2  where username= ?1", nativeQuery = true)
  void updateSubID(String username,String subId);


  @Modifying
  @Transactional
  @Query(value = "UPDATE users SET   jwttoken_status = 1,jwt_expire_time=DATE_ADD(now(), INTERVAL 10800 SECOND), jwt_create_time= now() where username= ?1", nativeQuery = true)
  void generatedToken(String username);


  @Query(value = "SELECT jwttoken_status FROM users where username = ?1", nativeQuery = true)
  String userLoginStatus(String username);

  @Query(value = "Select * From users  where jwt_expire_time > now() and username = ?1", nativeQuery = true)
  User tokenExpiry(String username);

  @Modifying
  @Transactional
  @Query(value = "UPDATE users SET jwttoken_auth=null,jwttoken_status =0 ,jwt_expire_time=null,jwt_create_time=null where username= ?1", nativeQuery = true)
  void userLogout(String name);

  @Modifying
  @Transactional
  @Query(value = "UPDATE users SET secret_key=?2 where username= ?1", nativeQuery = true)
  void  setSecretKey(String username,String key);

  @Modifying
  @Transactional
  @Query(value = "UPDATE users SET secret_key=null where username= ?1", nativeQuery = true)
  void  deleteSecretKey(String username);


  @Query(value = "SELECT secret_key FROM users where username = ?1", nativeQuery = true)
  String  getSecretKey(String username);

  @Query(value = "SELECT jwt_expire_time  FROM users where username = ?1", nativeQuery = true)
  java.sql.Timestamp getTimeDifferce(String username);

  //  @Query(value = "UPDATE devops_token SET token=?1 where id= 1", nativeQuery = true)


  @Modifying
  @Transactional
  @Query(value = "UPDATE users SET  devops_token =?2  where username= ?1", nativeQuery = true)
  void storeSecret(String username,String token);

  @Query(value = "SELECT devops_token FROM users where username = ?1", nativeQuery = true)
  String getDevopsToken(String username);


  @Modifying
  @Transactional
  @Query(value = "UPDATE users SET  sso_status =?2  where username= ?1", nativeQuery = true)
  void setSSOToken(String username, String token);


}
