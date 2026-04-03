package com.tastematch.service;

import com.tastematch.domain.AnonUser;
import com.tastematch.repository.AnonUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AnonUserRepository anonUserRepository;
    private final Random random = new Random();

    @Transactional
    public AnonUser createAnonymousUser() {
        String nickname = "취향인#" + String.format("%04d", random.nextInt(10000));
        String sessionToken = UUID.randomUUID().toString();

        AnonUser user = AnonUser.builder()
                .nickname(nickname)
                .sessionToken(sessionToken)
                .build();

        return anonUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<AnonUser> findBySessionToken(String sessionToken) {
        return anonUserRepository.findBySessionToken(sessionToken);
    }
}
