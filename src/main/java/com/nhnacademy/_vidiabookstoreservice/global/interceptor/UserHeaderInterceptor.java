package com.nhnacademy._vidiabookstoreservice.global.interceptor;

import com.nhnacademy._vidiabookstoreservice.global.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// 2. 인터셉터
@Component
public class UserHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdStr = request.getHeader("X-User-Id");
        String guestIdStr = request.getHeader("X-Guest-Id");
        String role = request.getHeader("X-User-Role");

        Long userId = null;
        Long guestId = null;

        try {
            if (userIdStr != null) userId = Long.valueOf(userIdStr);
            if (guestIdStr != null) guestId = Long.valueOf(guestIdStr);
        } catch (NumberFormatException e) {
            // 로깅 또는 무시
        }

        UserContext.set(userId, guestId, role);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear(); // ThreadLocal 메모리 누수 방지
    }
}
