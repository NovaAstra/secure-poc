package com.nebula.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "user_interface")
@Data
public class UserInterface implements Serializable {
  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;

  private Long interfaceId;

  private Integer total;

  private Integer remaining;

  private Integer status;

  private Date createTime;

  private Date updateTime;

  @TableLogic
  private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
