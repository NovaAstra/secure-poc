package com.nebula.core.model.vo;

import com.nebula.common.model.entity.Interface;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceVO extends Interface {

  private Integer total;

  private static final long serialVersionUID = 1L;
}