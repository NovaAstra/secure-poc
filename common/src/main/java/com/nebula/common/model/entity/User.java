package com.nebula.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
@TableName(value = "user")
@Data

public class User implements Serializable {

  @TableId(type = IdType.AUTO)
  private Long id;

  @TableField("user_account")
  private String userAccount;

  @TableField("user_password")
  private String userPassword;

  @TableField("access_key")
  private String accessKey;

  @TableField("secret_key")
  private String secretKey;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_delete")
  @TableLogic
  private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}