package com.nebula.core.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserVO implements Serializable {

  private Long id;

  private String userAccount;

  private String secretKey;

  private Date createTime;

  private Date updateTime;

  private static final long serialVersionUID = 1L;
}