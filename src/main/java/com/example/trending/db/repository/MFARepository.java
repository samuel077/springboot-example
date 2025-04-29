package com.example.trending.db.repository;

import com.example.trending.db.model.MFACode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MFARepository extends JpaRepository<MFACode, Long> {
    Optional<MFACode> findTopByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteByUserId(Long userId);
}