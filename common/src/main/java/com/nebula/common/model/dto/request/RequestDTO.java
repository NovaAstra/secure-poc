package com.nebula.common.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.Cookie;
import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO implements Serializable {
    private Cookie[] cookies;
}