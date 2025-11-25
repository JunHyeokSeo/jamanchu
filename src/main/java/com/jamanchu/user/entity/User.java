package com.jamanchu.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    private String email;
    private String password;
    private String nickname;
    private Boolean isDeleted;
    private Instant create_at;
    private Instant update_at;

    @Builder
    public User(
            String email,
            String password,
            String nickname
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.isDeleted = false;
        this.create_at = Instant.now();
        this.update_at = Instant.now();
    }
}
