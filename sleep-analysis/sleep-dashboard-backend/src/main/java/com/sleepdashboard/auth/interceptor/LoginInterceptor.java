package com.sleepdashboard.auth.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepdashboard.auth.util.JwtUtil;
import com.sleepdashboard.auth.util.UserContext;
import com.sleepdashboard.common.Result;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求（跨域预检）
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
            return true;
        }

        // 从 Header 提取 Bearer Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            DecodedJWT decodedJWT = JwtUtil.verifyToken(token);
            if (decodedJWT != null) {
                // 校验成功，绑定上下文
                UserContext.setUserId(JwtUtil.getUserId(decodedJWT));
                UserContext.setUsername(JwtUtil.getUsername(decodedJWT));
                return true;
            }
        }

        // 校验失败，返回 401 JSON 响应
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        PrintWriter writer = response.getWriter();
        Result<?> errorResult = Result.error(401, "未登录或登录已过期");
        writer.write(new ObjectMapper().writeValueAsString(errorResult));
        writer.flush();
        writer.close();
        
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后清理 ThreadLocal 防止内存泄漏
        UserContext.clear();
    }
}
