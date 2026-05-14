package com.manager.studio.managerstudio.repository;

import com.manager.studio.managerstudio.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TokenUserRepository extends JpaRepository<TokenEntity, String> {
    Optional<TokenEntity> findById(String id);
    // Dùng cho Filter (Access Token)
    Optional<TokenEntity> findByJti(String jti);

    // Dùng cho API Refresh Token
    Optional<TokenEntity> findByRefreshJti(String refreshJti);

    // Dùng khi Logout hoặc đổi pass: Thu hồi tất cả phiên của 1 user
    @Modifying
    @Query("UPDATE TokenEntity t SET t.revoked = true WHERE t.user.id = :userId")
    void revokeAllUserTokens(UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TokenEntity t WHERE t.expiryDate < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
