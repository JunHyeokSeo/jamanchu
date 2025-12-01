package com.jamanchu.reaction.entity;

import com.jamanchu.post.entity.Post;
import com.jamanchu.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reaction", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_post_user",
                columnNames = {"post_id", "user_id"}
        )
})
public class Reaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private ReactionType type;
    private Instant create_at;

    @Builder
    public Reaction(Post post, User user, ReactionType type) {
        this.post = post;
        this.user = user;
        this.type = type;
        this.create_at = Instant.now();
    }

    public Boolean isCreatedBy(User user) {
        return this.user.getId().equals(user.getId());
    }

    public void updateType(ReactionType type) {
        this.type = type;
    }
}

