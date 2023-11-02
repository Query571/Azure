/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAcceleratorLogin.repository;

import com.azureAcceleratorLogin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUserName(String userName);

  List<User> findByFirstNameOrLastName(String firstName, String lastName);

  @Modifying
  @Transactional
  @Query(value = "UPDATE users SET failed_attempts = failed_attempts + 1 where username= ?1", nativeQuery = true)
  void setInvalidCount(String username);

  @Modifying
  @Transactional
  @Query(value = "UPDATE users SET failed_attempts = 0 where username= ?1", nativeQuery = true)
  void setInvalidCountToZero(String username);


  @Modifying
  @Transactional
  @Query(value = "UPDATE users SET  jwttoken_auth = ?2, jwttoken_status = 1,jwt_expire_time=?3, jwt_create_time= now() where username= ?1", nativeQuery = true)
  void generatedToken(String username, String auth, String expireTime);

  @Query(value = "SELECT failed_attempts FROM users where username = ?1", nativeQuery = true)
  String getInvalidCount(String username);

  @Query(value = "SELECT jwttoken_status FROM users where username = ?1", nativeQuery = true)
  String getGenratedToken(String username);

  @Query(value = "select * from users where username=?1 or email=?1 LIMIT 1", nativeQuery = true)
  Optional<User> findByUserOrEmail(String username);

}
