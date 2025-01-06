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

  private String userAccount;

  private String userPassword;

  private String accessKey;

  private String secretKey;

  private LocalDateTime createTime;

  private LocalDateTime updateTime;

  @TableLogic
  private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}