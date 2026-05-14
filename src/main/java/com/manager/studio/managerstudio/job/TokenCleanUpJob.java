package com.manager.studio.managerstudio.job;


import com.manager.studio.managerstudio.repository.TokenUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanUpJob {

    private final TokenUserRepository tokenUserRepository;

    // Chạy vào lúc 2 giờ sáng mỗi ngày
    // Giây - Phút - Giờ - Ngày trong tháng - Tháng - Ngày trong tuần
    @Scheduled(cron = "0 2 * * * *")
    public void cleanUpExpiredTokens() {
        log.info("Start clean data token");

        try {
            LocalDateTime now = LocalDateTime.now();
            tokenUserRepository.deleteExpiredTokens(now);
            log.info("End clean data token success");
        } catch (Exception e) {
            log.error("Error clean token: ", e);
        }
    }
}
