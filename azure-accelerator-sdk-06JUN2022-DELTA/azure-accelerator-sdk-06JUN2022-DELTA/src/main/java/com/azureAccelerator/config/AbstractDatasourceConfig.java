/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.config;

import com.azureAccelerator.ApplicationProperties;
import com.azureAccelerator.dto.SecretsDto;
import com.azureAccelerator.util.VaultUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.function.Function;

public abstract class AbstractDatasourceConfig {

  private final ApplicationProperties applicationProperties;
  private final Environment environment;
  private final VaultUtil vaultUtil;

  AbstractDatasourceConfig(
      ApplicationProperties applicationProperties,
      Environment env,
      VaultUtil vaultUtil) {
    this.applicationProperties = applicationProperties;
    this.environment = env;
    this.vaultUtil = vaultUtil;
  }

  final HikariDataSource dataSource(
      Function<ApplicationProperties, String> mySqlSecretFunction) {
    if ("local".equals(environment.getActiveProfiles()[0])) {
      return DataSourceBuilder
          .create()
          .type(HikariDataSource.class)
          .build();
    } else {
      SecretsDto secret = vaultUtil.getSecrets(
          mySqlSecretFunction.apply(applicationProperties),
          applicationProperties.getVaultToken());
      return DataSourceBuilder
          .create()
          .type(HikariDataSource.class)
          .url(secret.getUrl())
          .username(secret.getUsername())
          .password(secret.getPassword())
          .driverClassName(applicationProperties.getDriverClass())
          .build();
    }
  }

  final LocalContainerEntityManagerFactoryBean entityManagerFactory(
      EntityManagerFactoryBuilder builder,
      DataSource dataSource,
      String packageName,
      String persistenceUnit) {
    return builder
        .dataSource(dataSource)
        //TODO: Convert the following to a true package reference and not string.
        .packages(packageName)
        .persistenceUnit(persistenceUnit)
        .build();
  }

  final PlatformTransactionManager jpaTransactionManager(
      EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }
}
