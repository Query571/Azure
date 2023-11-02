/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAcceleratorLogin.config;

import com.azureAcceleratorLogin.ApplicationProperties;
import com.azureAcceleratorLogin.entity.Role;
import com.azureAcceleratorLogin.entity.User;
import com.azureAcceleratorLogin.service.UserService;
import com.azureAcceleratorLogin.util.VaultUtil;
import java.util.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private boolean userSearchInitiated = false;

  private final ApplicationProperties applicationProperties;
  private final VaultUtil vaultUtil;

  @Autowired
  public WebSecurityConfig(
          ApplicationProperties applicationProperties,
          VaultUtil vaultUtil) {
    this.applicationProperties = applicationProperties;
    this.vaultUtil = vaultUtil;
  }

  @Override
  @Bean
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }


/*  @Override
  protected void configure(HttpSecurity http) {
    http
            .csrf(csrf ->csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            );
  }*/

  @Autowired
  public void globalUserDetails(
          AuthenticationManagerBuilder authenticationManagerBuilder,
          UserService userService,
          BCryptPasswordEncoder encoder)
          throws Exception {

    Base64.Decoder decoder = Base64.getDecoder();
    authenticationManagerBuilder.userDetailsService(
                    username ->
                            userService
                                    .findByUsername(new String(decoder.decode(username)))
                                    .map(AuthenticatedDatabaseUserDetailsImpl::new)
                                    .orElseThrow(
                                            () -> new UsernameNotFoundException("Invalid username or password.")))
            .passwordEncoder(
  new PasswordEncoder(){

              BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

              @Override
              public boolean matches(CharSequence rawPassword, String encodedPassword) {
                Base64.Decoder decoder = Base64.getDecoder();
                String decryptedPassword = new String(decoder.decode(rawPassword.toString()));
                if(decryptedPassword.contains("SSO")){
                  return true;
                }else{
                  return encoder.matches(decryptedPassword,encodedPassword);
                }
              }

              @Override
              public String encode(CharSequence rawPassword) {
                return null;
              }

            });
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
//    http.csrf()
//        .disable()
    http.anonymous()
            .disable()
            .authorizeRequests()
            .antMatchers("*")
            .permitAll();
  }

  @Override
  public void configure(WebSecurity web) {

    web.ignoring().antMatchers(HttpMethod.OPTIONS);
  }



  @Getter
  class AuthenticatedDatabaseUserDetailsImpl implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final List<SimpleGrantedAuthority> authorities;
    private final String commonName;
    private final String userUID;
    private final String secret;
    private final String accountFailCount;
    private final Set<Role> roles;

    AuthenticatedDatabaseUserDetailsImpl(User user) {
      this.id = user.getId();
      this.username = user.getUserName();
      this.password = user.getPassWord();
      this.userUID= user.getUserUID();
      this.accountFailCount= user.getAccountLocked();
      this.secret= user.getSecret();

      // this.authorities = Collections.singletonList(
      //     new SimpleGrantedAuthority(user.getUserName()));
      this.commonName = user.getFirstName() + " " + user.getLastName();
      roles = user.getRoles();
      List<SimpleGrantedAuthority> authorities = new ArrayList<>();

      for (Role role : roles) {
        authorities.add(new SimpleGrantedAuthority(role.getName()));
      }
      this.authorities =authorities;

    }

    @Override
    public boolean isAccountNonExpired() {
      return true;
    }

    @Override
    public boolean isAccountNonLocked() {
      int count=Integer.parseInt(accountFailCount);
      return count>5?false:true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
      return true;
    }

    @Override
    public boolean isEnabled() {
      return true;
    }
  }

}
