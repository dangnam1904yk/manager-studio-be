package com.manager.studio.managerstudio.entity;

import com.manager.studio.managerstudio.util.UUIDv7;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_tokens", indexes = {
        @Index(name = "idx_token", columnList = "jti"),
        @Index(name = "idx_token_refresh", columnList = "refreshJti"),
        @Index(name = "idx_expiryDate", columnList = "expiryDate"),
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenEntity {
    @Id
    @UUIDv7
    private UUID id;

    private String jti;

    private String refreshJti;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user; // Lưu userId để biết token này của ai

    private String deviceType; // Để phân biệt Mobile/Web
    private LocalDateTime expiryDate;

    private boolean revoked; // Đánh dấu thu hồi
}
