package com.nebula.common.utils;

import cn.hutool.core.util.StrUtil;
import com.nebula.common.model.dto.request.RequestDTO;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CookieUtil {

    private final static String COOKIE_NAME = "nebula_login_token";
    private static final String COOKIE_PATH = "/";
    private static final Integer COOKIE_MAX_AGE = 60 * 60; // 1 hour

    public static void writeLoginToken(String token, HttpServletResponse response) {
        if (StrUtil.isBlank(token) || response == null) {
            return;
        }

        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(COOKIE_MAX_AGE);

        response.addCookie(cookie);
    }

    public static String readLoginToken(HttpServletRequest request) {
        return getCookieValue(request, COOKIE_NAME);
    }

    public static String readLoginToken(RequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }

        Cookie[] cookies = requestDTO.getCookies();
        return getCookieValueFromArray(cookies, COOKIE_NAME);
    }

    public static void deleteLoginToken(HttpServletRequest request, HttpServletResponse response) {
        if (request == null || response == null) {
            return;
        }

        Cookie cookie = getCookie(request, COOKIE_NAME);
        if (cookie != null) {
            cookie.setMaxAge(0);
            cookie.setPath(COOKIE_PATH);

            response.addCookie(cookie);
        }
    }

    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie cookie = getCookie(request, cookieName);
        return cookie != null ? cookie.getValue() : null;
    }

    public static Cookie getCookie(HttpServletRequest request, String cookieName) {
        if (request == null || StrUtil.isBlank(cookieName)) {
            return null;
        }

        Cookie[] cookies = request.getCookies();
        return getCookieFromArray(cookies, cookieName);
    }

    private static String getCookieValueFromArray(Cookie[] cookies, String cookieName) {
        Cookie cookie = getCookieFromArray(cookies, cookieName);
        return cookie != null ? cookie.getValue() : null;
    }

    private static Cookie getCookieFromArray(Cookie[] cookies, String cookieName) {
        if (cookies == null || StrUtil.isBlank(cookieName)) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }
}
