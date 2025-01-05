package com.nebula.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.common.model.entity.UserInterface;

import java.util.List;

public interface UserInterfaceMapper extends BaseMapper<UserInterface> {

  List<UserInterface> listTopInvokeInterfaceInfo(int limit);

  boolean updateRemainingByIncrement(Long id, int leftNum, int increment);
}
