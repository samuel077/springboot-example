package com.example.trending.db.repository;

import com.example.trending.db.enums.TokenType;
import com.example.trending.db.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);

    List<Token> findAllByUserIdAndExpiredFalseAndRevokedFalseAndTokenType(Long userId, TokenType tokenType);

    @Modifying
    @Query("UPDATE Token t SET t.revoked = true WHERE t.userId = :userId AND t.tokenType = :type")
    void revokeAllTokensByUserIdAndType(@Param("userId") Long userId, @Param("type") TokenType type);
}