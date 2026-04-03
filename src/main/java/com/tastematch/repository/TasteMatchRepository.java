package com.tastematch.repository;

import com.tastematch.domain.AnonUser;
import com.tastematch.domain.TasteMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TasteMatchRepository extends JpaRepository<TasteMatch, Long> {
    List<TasteMatch> findByUser1OrUser2(AnonUser user1, AnonUser user2);
    Optional<TasteMatch> findByUser1AndUser2(AnonUser user1, AnonUser user2);
}
