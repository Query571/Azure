/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.config;

import org.modelmapper.ModelMapper;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

   @Bean
  public BCryptPasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }

  @Bean
  @LoadBalanced        // Load balance between service instances running at different ports.
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }


}
