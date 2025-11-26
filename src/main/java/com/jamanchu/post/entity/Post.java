package com.jamanchu.post.entity;

import com.jamanchu.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;
    String content;
    Instant create_at;
    Instant update_at;
    Boolean isDeleted;

    @Builder
    public Post(User user, String content) {
        this.user = user;
        this.content = content;
        this.create_at = Instant.now();
        this.update_at = Instant.now();
        this.isDeleted = false;
    }

    public void update(String content) {
        this.content = content;
        this.update_at = Instant.now();
    }

    public void delete() {
        this.isDeleted = true;
        this.update_at = Instant.now();
    }
}
