package com.hometech.hometech.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(true);
        
        // Lưu session ID vào response header để client có thể sử dụng
        response.setHeader("X-Session-ID", session.getId());
        
        // Lưu session ID vào request attribute để các controller có thể sử dụng
        request.setAttribute("sessionId", session.getId());
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Có thể thêm logic sau khi request hoàn thành
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Cập nhật last accessed time
            session.getLastAccessedTime();
        }
    }
}


