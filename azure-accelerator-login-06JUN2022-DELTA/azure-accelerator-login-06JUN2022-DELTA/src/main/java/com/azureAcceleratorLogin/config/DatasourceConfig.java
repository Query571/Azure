/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAcceleratorLogin.config;

import com.azureAcceleratorLogin.ApplicationProperties;
import com.azureAcceleratorLogin.entity.AbstractInternalEntity;
import com.azureAcceleratorLogin.util.VaultUtil;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    entityManagerFactoryRef = DatasourceConfig.ENTITY_MANAGER,
    transactionManagerRef = DatasourceConfig.TRANSACTION_MANAGER,
    basePackages = DatasourceConfig.BASE_REPOSITORY_PACKAGE)
public class DatasourceConfig extends AbstractDatasourceConfig {

  static final String ENTITY_MANAGER = "entityManager";
  static final String TRANSACTION_MANAGER = "transactionManager";
  static final String BASE_REPOSITORY_PACKAGE = "com.azureAcceleratorLogin.repository";
  private static final String SPRING_DATASOURCE = "spring.datasource";
  private static final String BASE_ENTITY_PACKAGE =
      AbstractInternalEntity.class.getPackage().getName();
  private static final String PERSISTENCE_UNIT = "persistenceunit";

  @Autowired
  public DatasourceConfig(
      ApplicationProperties applicationProperties,
      Environment env,
      VaultUtil vaultUtil) {
    super(applicationProperties, env, vaultUtil);
  }

  @Primary
  @Bean
  @ConfigurationProperties(prefix = SPRING_DATASOURCE)
  public DataSource internalDataSource() {
    return dataSource(ApplicationProperties::getVaultMysqlSecret);
  }

  @Primary
  @Bean(name = ENTITY_MANAGER)
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
      EntityManagerFactoryBuilder builder) {
    return entityManagerFactory(
        builder,
        internalDataSource(),
            BASE_ENTITY_PACKAGE,
            PERSISTENCE_UNIT);
  }

  @Primary
  @Bean(name = TRANSACTION_MANAGER)
  public PlatformTransactionManager internalTransactionManager(
      @Qualifier(ENTITY_MANAGER) EntityManagerFactory entityManagerFactory) {
    return transactionManager(entityManagerFactory);
  }
}
