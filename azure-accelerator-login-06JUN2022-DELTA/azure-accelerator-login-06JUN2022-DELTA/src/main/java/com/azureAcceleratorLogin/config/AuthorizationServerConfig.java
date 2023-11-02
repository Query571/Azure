/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAcceleratorLogin.config;

import com.azureAcceleratorLogin.ApplicationProperties;
import com.azureAcceleratorLogin.entity.User;
import com.azureAcceleratorLogin.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Base64;
import java.util.Optional;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

  private static final String AUTHORIZATION_CODE = "authorization_code";
  private static final String REFRESH_TOKEN = "refresh_token";
  private static final String IMPLICIT = "implicit";
  private static final String SCOPE_READ = "read";
  private static final String SCOPE_WRITE = "write";
  private static final String TRUST = "trust";
  private static final int ACCESS_TOKEN_VALIDITY_SECONDS = 180 * 60;
  private static final int REFRESH_TOKEN_VALIDITY_SECONDS = 90 * 60;
  private static final String ROLE= "role";
  private static final String COMMON_NAME = "commonName";
  private static final String USER_NAME = "userName";
  private static final String SECRET= "secret";

  private static final String USER_ID = "id";
  private static final String USER_UID = "user_uid";

  private final ApplicationProperties applicationProperties;
  private final AuthenticationManager authenticationManager;
  private final TokenStore tokenStore;


  @Autowired
  UserServiceImpl userServiceImpl;

  @Autowired
  public AuthorizationServerConfig(
          ApplicationProperties applicationProperties,
          AuthenticationManager authenticationManager,
          TokenStore tokenStore) {
    this.applicationProperties = applicationProperties;
    this.authenticationManager = authenticationManager;
    this.tokenStore = tokenStore;
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer configurer) throws Exception {
    configurer
            .inMemory()
            .withClient(applicationProperties.getClientId())
            .secret(applicationProperties.getClientSecret())
            .authorizedGrantTypes(applicationProperties.getPass(), AUTHORIZATION_CODE, REFRESH_TOKEN, IMPLICIT)
            .scopes(SCOPE_READ, SCOPE_WRITE, TRUST)
            .accessTokenValiditySeconds(ACCESS_TOKEN_VALIDITY_SECONDS)
            .refreshTokenValiditySeconds(REFRESH_TOKEN_VALIDITY_SECONDS);
  }

  @Bean
  public JwtAccessTokenConverter accessTokenConverter() {

    final String[] username = new String[1];
    String count=null;
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter() {
      @Override
      public OAuth2AccessToken enhance(
              OAuth2AccessToken accessToken,
              OAuth2Authentication authentication) {
        DefaultOAuth2AccessToken customAccessToken =
                new DefaultOAuth2AccessToken(accessToken);

        Object principal = authentication.getPrincipal();
        String token = null;

        if (principal instanceof WebSecurityConfig.AuthenticatedDatabaseUserDetailsImpl) {
          WebSecurityConfig.AuthenticatedDatabaseUserDetailsImpl authenticatedDatabaseUserDetailsImpl =
                  ((WebSecurityConfig.AuthenticatedDatabaseUserDetailsImpl) principal);
          customAccessToken.getAdditionalInformation()
                  .put(COMMON_NAME, authenticatedDatabaseUserDetailsImpl.getCommonName());
          customAccessToken.getAdditionalInformation()
                  .put(ROLE, authenticatedDatabaseUserDetailsImpl.getRoles());
          customAccessToken.getAdditionalInformation()
                  .put(USER_ID, authenticatedDatabaseUserDetailsImpl.getId());
          customAccessToken.getAdditionalInformation()
                  .put(USER_NAME, authenticatedDatabaseUserDetailsImpl.getUsername());
          customAccessToken.getAdditionalInformation()
                  .put(SECRET, authenticatedDatabaseUserDetailsImpl.getSecret());
          customAccessToken.getAdditionalInformation()
                  .put(USER_UID, authenticatedDatabaseUserDetailsImpl.getUserUID());
          username[0] = authenticatedDatabaseUserDetailsImpl.getUsername();
          String count = userServiceImpl.getInvalidCount(authenticatedDatabaseUserDetailsImpl.getUsername());
        }
     //   userServiceImpl.generatedToken(String.valueOf(username[0]),
     //             String.valueOf(super.enhance(customAccessToken, authentication)));

        return super.enhance(customAccessToken, authentication);
      }
    };

    converter.setSigningKey(applicationProperties.getSigningKey());

    return converter;
  }


  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
    endpoints.tokenStore(tokenStore)
            .authenticationManager(authenticationManager)
            .accessTokenConverter(accessTokenConverter());
  }

  @EventListener
  public void authenticationFailed(AuthenticationFailureBadCredentialsEvent event) {
//    System.out.println("username failed -------->"+ (String) event.getAuthentication().getPrincipal());

    String username = (String) event.getAuthentication().getPrincipal();
    Base64.Decoder decoder = Base64.getDecoder();
    username= new String(decoder.decode(username));

    userServiceImpl.setInvalidCount(username);
    String count=userServiceImpl.getInvalidCount(username);

  }


  @EventListener
  public void authenticationSuccess(AuthenticationSuccessEvent event) {

    //   AuthenticationSuccessEvent event = (AuthenticationSuccessEvent) appEvent;
    //  UserDetails userDetails = (UserDetails) event.getAuthentication().getPrincipal();

  }

  @Component
  public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {


    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent authenticationSuccessEvent) {

      try {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
          WebSecurityConfig.AuthenticatedDatabaseUserDetailsImpl data=null;
          try {
            data =(WebSecurityConfig.AuthenticatedDatabaseUserDetailsImpl) authenticationSuccessEvent.getAuthentication().getPrincipal();
          } catch (ClassCastException exc) {

          }
          if (data != null) {
            String user = data.getUsername();
            String user1 = data.getCommonName();
            userServiceImpl.setInvalidCountToZero(data.getUsername());
          }

        }
      }catch (Exception e){
      }
    }
  }

}