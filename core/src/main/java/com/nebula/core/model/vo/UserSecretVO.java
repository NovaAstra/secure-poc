package com.nebula.core.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserSecretVO implements Serializable {
  private static final long serialVersionUID = 6703326011663561616L;

  private String ak;
  private String sk;
}
