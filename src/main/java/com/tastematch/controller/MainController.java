package com.tastematch.controller;

import com.tastematch.domain.AnonUser;
import com.tastematch.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;

    @GetMapping("/")
    public String index(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            Optional<AnonUser> userOpt = userService.findBySessionToken(token);
            if (userOpt.isPresent()) {
                return "redirect:/feed";
            }
        }
        return "index";
    }

    @PostMapping("/join")
    public String join(HttpServletResponse response) {
        AnonUser user = userService.createAnonymousUser();

        Cookie cookie = new Cookie("TASTE_TOKEN", user.getSessionToken());
        cookie.setPath("/");
        cookie.setMaxAge(604800);
        response.addCookie(cookie);

        return "redirect:/feed";
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
