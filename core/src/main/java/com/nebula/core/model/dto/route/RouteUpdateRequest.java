package com.nebula.core.model.dto.route;

import java.io.Serializable;

import lombok.Data;

@Data
public class RouteUpdateRequest implements Serializable {
  private Long id;

  private String code;

  private String name;

  private String description;

  private String url;

  private String method;

  private String sdk;

  private static final long serialVersionUID = 1L;
}
