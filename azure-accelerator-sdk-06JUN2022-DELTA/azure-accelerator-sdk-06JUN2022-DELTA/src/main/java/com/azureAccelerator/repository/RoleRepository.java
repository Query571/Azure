/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.repository;

import com.azureAccelerator.entity.Role;
import com.azureAccelerator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String user);

//  Optional<User> findByUserName(String userName);

 // List<User> findByFirstNameOrLastName(String firstName, String lastName);
}
