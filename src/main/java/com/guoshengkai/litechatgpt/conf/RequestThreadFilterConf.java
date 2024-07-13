package com.guoshengkai.litechatgpt.conf;

import com.guoshengkai.litechatgpt.core.cache.CacheUtil;
import com.guoshengkai.litechatgpt.core.cache.Keys;
import com.guoshengkai.litechatgpt.core.util.ThreadUtil;
import com.guoshengkai.litechatgpt.entity.User;
import com.guoshengkai.litechatgpt.exception.AccessOAuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2021 HEBEI CLOUD IOT FACTORY BIGDATA CO.,LTD.
 * Legal liability shall be investigated for unauthorized use
 *
 * @Author: Guo Shengkai
 * @Date: Create in 2021/04/08 9:46
 */
public class RequestThreadFilterConf implements HandlerInterceptor {


    protected Logger logger = LoggerFactory.getLogger(RequestThreadFilterConf.class);

    Map<String, List<Date>> requestMap = new HashMap<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String reqHeader = request.getHeader("Access-Control-Request-Headers");
        response.setStatus(HttpStatus.OK.value());
        if (reqHeader != null){
            response.setHeader("Access-Control-Allow-Headers", reqHeader);
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())){
            response.setStatus(204);
            return false;
        }

        String token = getRequestToken(request);
        Keys tokenKey = Keys.keys("TOKEN", token);
        // 解析token
        if (StringUtils.hasText(token)){
            ThreadUtil.setToken(token);
            ThreadUtil.setUserId(CacheUtil.getObject(tokenKey, Integer.class));
            if (null != ThreadUtil.getUserId() && 0 != ThreadUtil.getUserId()){
                CacheUtil.pushObject(tokenKey, ThreadUtil.getUserId(), 60 * 60 * 24 * 7);
            }

        }
        if (handler instanceof HandlerMethod method) {
            NoLogin methodAnnotation = method.getMethodAnnotation(NoLogin.class);
            if (null == methodAnnotation){
                if (null == token){
                    throw new AccessOAuthException("请先认证");
                }
                if (null == ThreadUtil.getUserId() || 0 == ThreadUtil.getUserId()){
                    throw new AccessOAuthException("认证信息已过期，请重新认证");
                }
            }
        }

        // 初始化上下文
        return true;
    }


    /**
     * 获取请求信息中的Token
     * @param request
     *      请求信息
     * @return
     */
    private String getRequestToken(HttpServletRequest request){
        String token = request.getHeader("token");

        if (StringUtils.isEmpty(token)){
            token = request.getHeader("access_token");
        }
        if (StringUtils.isEmpty(token)){
            token = request.getHeader("access-token");
        }
        if (StringUtils.isEmpty(token)){
            token = request.getHeader("authorization");
        }

        if (StringUtils.isEmpty(token)){
            token = request.getParameter("token");
        }
        if (StringUtils.isEmpty(token)){
            token = request.getParameter("access_token");
        }
        if (StringUtils.isEmpty(token)){
            token = request.getParameter("access-token");
        }

        if (!StringUtils.isEmpty(token) && token.startsWith("Bearer")){
            token = token.substring(7);
        }
        return token;
    }
}
