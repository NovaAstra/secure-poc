package com.nebula.core.model.dto.user;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserRegisterRequest implements Serializable {
  private static final long serialVersionUID = 3191241716373120793L;

  private String account;

  private String password;

  private String confirmation;
}
