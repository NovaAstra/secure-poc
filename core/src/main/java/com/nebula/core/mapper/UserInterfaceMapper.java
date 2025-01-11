package com.nebula.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.common.model.entity.UserRoute;

import java.util.List;

public interface UserInterfaceMapper extends BaseMapper<UserRoute> {

  List<UserRoute> listTopInvokeInterfaceInfo(int limit);

  boolean updateRemainingByIncrement(Long id, int leftNum, int increment);
}
