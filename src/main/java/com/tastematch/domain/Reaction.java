package com.tastematch.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reaction",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "reactor_id"}))
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reactor_id", nullable = false)
    private AnonUser reactor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
