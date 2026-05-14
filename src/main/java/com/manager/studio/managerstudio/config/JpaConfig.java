package com.manager.studio.managerstudio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // Cấu hình thêm cho AuditorAware (người tạo) nếu cần
}