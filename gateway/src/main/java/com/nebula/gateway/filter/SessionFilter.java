package com.nebula.gateway.filter;

import javax.servlet.annotation.WebFilter;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import lombok.extern.slf4j.Slf4j;

@Component
@WebFilter(urlPatterns = "/*", filterName = "sessionExpireFilter")
@Slf4j
@CrossOrigin(origins = "*")
public class SessionFilter {
  
}
