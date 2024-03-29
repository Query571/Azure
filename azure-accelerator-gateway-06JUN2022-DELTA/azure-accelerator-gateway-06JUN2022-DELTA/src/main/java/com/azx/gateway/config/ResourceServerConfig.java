/*
 * package com.azx.gateway.config;
 * 
 * 
 * import org.springframework.context.annotation.Bean; import
 * org.springframework.context.annotation.Configuration; import
 * org.springframework.security.config.annotation.web.builders.HttpSecurity;
 * import
 * org.springframework.security.oauth2.config.annotation.web.configuration.
 * EnableResourceServer; import
 * org.springframework.security.oauth2.config.annotation.web.configuration.
 * ResourceServerConfigurerAdapter; import
 * org.springframework.transaction.annotation.EnableTransactionManagement;
 * import org.springframework.web.cors.CorsConfiguration; import
 * org.springframework.web.cors.UrlBasedCorsConfigurationSource; import
 * org.springframework.web.filter.CorsFilter;
 * 
 * @Configuration
 * 
 * @EnableResourceServer
 * 
 * @EnableTransactionManagement(proxyTargetClass = true) public class
 * ResourceServerConfig extends ResourceServerConfigurerAdapter {
 * 
 * 
 * 
 * 
 * @Override public void configure(HttpSecurity http) throws Exception {
 * http.cors(); }
 * 
 * @Bean public CorsFilter corsFilter() { UrlBasedCorsConfigurationSource source
 * = new UrlBasedCorsConfigurationSource(); CorsConfiguration config = new
 * CorsConfiguration(); config.setAllowCredentials(true); //
 * config.addAllowedOriginPattern("*"); config.addAllowedOrigin("*");
 * config.addAllowedHeader("*"); config.addAllowedMethod("*");
 * source.registerCorsConfiguration("/**", config);
 * 
 * return new CorsFilter(source); }
 * 
 * 
 * 
 * 
 * }
 */