package com.nebula.core.model.dto.route;

import java.io.Serializable;

import lombok.Data;

@Data
public class RouteAddRequest implements Serializable {
  private String code;

  private String name;

  private String description;

  private String url;

  private String requestParams;

  private String requestHeader;

  private String responseHeader;

  private String method;
}
