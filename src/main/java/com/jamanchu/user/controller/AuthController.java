package com.jamanchu.user.controller;

import com.jamanchu.user.dto.request.LoginRequest;
import com.jamanchu.user.dto.request.SignupRequest;
import com.jamanchu.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<@NonNull String> signup(
            @RequestBody SignupRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.signup(req.email(), req.password()));
    }

    @PostMapping("/login")
    public ResponseEntity<@NonNull Boolean> login(
            HttpServletRequest request,
            @RequestBody LoginRequest req) {
        return ResponseEntity.ok(userService.login(request, req.email(), req.password()));
    }
}
