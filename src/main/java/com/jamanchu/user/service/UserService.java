package com.jamanchu.user.service;

import com.jamanchu.user.entity.User;
import com.jamanchu.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public String signup(String email, String password) {
        Optional<User> findUser = userRepository.findByEmailAndIsDeletedIsFalse(email);
        if (findUser.isPresent())
            throw new IllegalStateException("중복된 유저 ID 입니다.");

        String encodedPassword = passwordEncoder.encode(password);
        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(generateAutoNickName())
                .build();

        return userRepository.save(user).getId().toString();
    }

    // 로그인
    @Transactional(readOnly = true)
    public Boolean login(HttpServletRequest request, String email, String password) {
        User findUser = userRepository.findByEmailAndIsDeletedIsFalse(email)
                .orElseThrow(() -> new BadCredentialsException("유효하지 않은 로그인 정보"));

        if (passwordEncoder.matches(password, findUser.getPassword())) {
            HttpSession session = request.getSession();
            session.setAttribute("userId", findUser.getId());
            return true;
        }

        throw new BadCredentialsException("유효하지 않은 로그인 정보");
    }

    private String generateAutoNickName() {
        return "준기사" + UUID.randomUUID().toString().substring(0, 8);
    }
}
