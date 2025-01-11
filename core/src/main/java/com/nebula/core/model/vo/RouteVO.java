package com.nebula.core.model.vo;

import com.nebula.common.model.entity.Route;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RouteVO extends Route {

  private Integer total;

  private static final long serialVersionUID = 1L;
}