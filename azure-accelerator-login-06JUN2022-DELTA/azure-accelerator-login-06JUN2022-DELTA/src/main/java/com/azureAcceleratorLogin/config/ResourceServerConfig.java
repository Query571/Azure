/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAcceleratorLogin.config;

import com.azureAcceleratorLogin.ApplicationProperties;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableResourceServer
@EnableTransactionManagement(proxyTargetClass = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

  private final ApplicationProperties applicationProperties;
  private final DefaultTokenServices tokenServices;

  @Autowired
  public ResourceServerConfig(
      ApplicationProperties applicationProperties,
      DefaultTokenServices tokenServices) {
    this.applicationProperties = applicationProperties;
    this.tokenServices = tokenServices;
  }

  @Override
  public void configure(ResourceServerSecurityConfigurer resources) {
    resources
        .resourceId(applicationProperties.getResourceIds())
        .tokenServices(tokenServices);
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.cors()
        .and()
        .requestMatchers()
        .and()
        .authorizeRequests()
        .antMatchers()
        .permitAll()
        .anyRequest()
        .authenticated();
  }

  /*@Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
   // config.addAllowedOriginPattern("*");
    config.addAllowedOrigin("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    source.registerCorsConfiguration("/**", config);

    return new CorsFilter(source);
  }*/


}

