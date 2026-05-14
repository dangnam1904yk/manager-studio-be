package com.manager.studio.managerstudio.util;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private HttpStatus status;
    private String code;
    private String message;
    private String path;
}
