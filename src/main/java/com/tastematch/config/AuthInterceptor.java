package com.tastematch.config;

import com.tastematch.domain.AnonUser;
import com.tastematch.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = extractToken(request);

        if (token == null) {
            response.sendRedirect("/");
            return false;
        }

        Optional<AnonUser> userOpt = userService.findBySessionToken(token);
        if (userOpt.isEmpty()) {
            response.sendRedirect("/");
            return false;
        }

        request.setAttribute("currentUser", userOpt.get());
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if ("TASTE_TOKEN".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
