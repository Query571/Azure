/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAcceleratorLogin.config;

import java.util.Properties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ErrorMessagesConfig {

  private final Properties configProp = new Properties();

  /*public ErrorMessagesConfig() {
    try {
      configProp.load(
          this.getClass()
              .getClassLoader()
              .getResourceAsStream("CustomErrorMessages.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }*/

  public String getProperty(String key) {
    return configProp.getProperty(key);
  }
}