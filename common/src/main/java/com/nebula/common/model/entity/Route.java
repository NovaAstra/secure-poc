package com.nebula.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "route")
@Data
public class Route implements Serializable {
  @TableId(type = IdType.AUTO)
  private Long id;

  private String code;

  private String name;

  private String url;

  private String params;

  private String query;

  private String header;

  private String method;

  private Integer status;

  private Date createTime;

  private Date updateTime;

  @TableLogic
  private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
