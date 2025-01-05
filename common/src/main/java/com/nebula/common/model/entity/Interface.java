package com.nebula.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "interface")
@Data
public class Interface implements Serializable {
  @TableId(type = IdType.AUTO)
  private String id;

  private String name;

  private String url;

  private String requestParams;

  private String requestHeader;

  private String responseHeader;

  private Integer status;

  private String method;

  private Date createTime;

  private Date updateTime;

  @TableLogic
  private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
