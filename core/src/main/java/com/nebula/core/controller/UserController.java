package com.nebula.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nebula.common.common.BaseResponse;
import com.nebula.common.common.ErrorCode;
import com.nebula.common.utils.ResultUtils;
import com.nebula.common.exception.BusinessException;
import com.nebula.core.model.dto.user.UserRegisterRequest;
import com.nebula.core.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("user")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("register")
  public BaseResponse<String> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
    if (userRegisterRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    String userAccount = userRegisterRequest.getUserAccount();
    String userPassword = userRegisterRequest.getUserPassword();
    String confirmPassword = userRegisterRequest.getConfirmPassword();
    String code = userRegisterRequest.getCode();

    String result = userService.userRegister(userAccount, userPassword, confirmPassword, code);
    return ResultUtils.success(result);
  }
}
