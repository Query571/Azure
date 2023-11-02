package com.azx.gateway.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableResourceServer
@PropertySource(value = "classpath:application.properties")
public class GatewayConfiguration extends ResourceServerConfigurerAdapter {

  private final  static String ADMIN="Admin";
  private final  static String OPERATOR="Operator";
  private final  static String AUDITOR="Auditor";

  @Value("${security.signing-key}")
   String signingkey;
  @Value("${zuul.prefix}")
  String zuulPrefix;


  @Override
  public void configure(final HttpSecurity http) throws Exception {
    http.authorizeRequests()
            .antMatchers(zuulPrefix+"/oauth/**")
            .permitAll()

            .antMatchers(zuulPrefix+"/azxsdk/azure/addUser")
            .hasAnyAuthority("Admin")
            .antMatchers(zuulPrefix+"/azxsdk/azure/editUser")
            .hasAnyAuthority("Admin")
            .antMatchers(zuulPrefix+"/azxsdk/azure/deleteUser")
            .hasAnyAuthority("Admin")
            .antMatchers(zuulPrefix+"/azxsdk/azure/getAllUser")
            .hasAnyAuthority("Admin")
            .antMatchers(zuulPrefix+"/azxsdk/azure/updatePassword")
            .hasAnyAuthority("Admin","Operator","Auditor")

            .antMatchers(zuulPrefix+"/azxsdk/azure/startWebApp")
            .hasAnyAuthority("Admin","Operator")
            .antMatchers(zuulPrefix+"/azxsdk/azure/stopWebApp")
            .hasAnyAuthority("Admin","Operator")
            .antMatchers(zuulPrefix+"/azxsdk/azure/restartWebApp")
            .hasAnyAuthority("Admin","Operator")
            .antMatchers(zuulPrefix+"/azxsdk/azure/deleteWebApp?")
            .hasAnyAuthority("Admin","Operator")




            .antMatchers(HttpMethod.POST,zuulPrefix+"/azxsdk/azure/**")
            .hasAnyAuthority("Admin","Operator")
            .antMatchers(HttpMethod.PUT,zuulPrefix+"/azxsdk/azure/**")
            .hasAnyAuthority("Admin","Operator")
            .antMatchers(HttpMethod.DELETE,zuulPrefix+"/azxsdk/azure/**")
            .hasAnyAuthority("Admin","Operator")
            .antMatchers(HttpMethod.GET,zuulPrefix+"/azxsdk/azure/**")
            .hasAnyAuthority("Admin","Operator","Auditor")
            .antMatchers(zuulPrefix+"/**")
            .authenticated();

    //            .antMatchers(zuulPrefix+"/azxsdk/azure/getListOfAssignPolicies")
//            .hasRole("USER")
//        .antMatchers(zuulPrefix+"/azxsdk/azure/getInitiativePolicies")
//            .hasRole("ADMIN");


  }
  
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		//configuration.setAllowedOrigins(Arrays.asList("http://3.108.94.64"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE")); 
		//configuration.setExposedHeaders(Arrays.asList("Authorization","content-type"));
		configuration.setAllowCredentials(true);
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "content-type"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
	 



  @Override
  public void configure(ResourceServerSecurityConfigurer config) {
    config.tokenServices(tokenServices());
  }

  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(accessTokenConverter());
  }

  @Bean
  public JwtAccessTokenConverter accessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    converter.setSigningKey(signingkey);
    return converter;
  }

  @Bean
  @Primary
  public DefaultTokenServices tokenServices() {
    DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
    defaultTokenServices.setTokenStore(tokenStore());
    return defaultTokenServices;
  }
}
