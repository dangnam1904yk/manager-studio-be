package com.manager.studio.managerstudio.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Getter
@Slf4j
public class BusinessException extends  Exception{

    private final HttpStatus errorCode;

    public BusinessException(String message, HttpStatus errorCode) {
        super(message);
        log.warn("BusinessException : {}",message);
        this.errorCode = errorCode;
    }

    public BusinessException(String message) {
        super(message);
        log.warn("BusinessException : {}",message);
        this.errorCode = HttpStatus.BAD_REQUEST;
    }
}
