package com.nebula.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "user_route")
@Data
public class UserRoute implements Serializable {
  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;

  private Long routeId;

  private Integer maxCalls;

  private Integer callCount;

  private Integer status;

  private Date createTime;

  private Date updateTime;

  @TableLogic
  private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
