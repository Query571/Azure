/*
 * Copyright (C) 2018- Amorok.io
 * All rights reserved.
 */

package com.azureAccelerator.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class AzureAcltrRuntimeException extends RuntimeException {

  private static final long serialVersionUID = -9034337190745842940L;

  private final Integer messageId;
  private final String detailedMessage;
  private final HttpStatus statusCode;
  private final Throwable cause;

  public AzureAcltrRuntimeException(
      String msg,
      Integer messageId,
      String detailedMessage,
      HttpStatus internalServerError) {
    this(msg, messageId, detailedMessage, internalServerError, null);
  }

  public AzureAcltrRuntimeException(
      String msg,
      Integer messageId,
      String detailedMessage,
      HttpStatus internalServerError,
      Throwable cause) {
    super(msg != null ? msg : "", cause);
    this.messageId = messageId;
    this.detailedMessage = detailedMessage;
    this.statusCode = internalServerError;
    this.cause = cause;
  }
}
