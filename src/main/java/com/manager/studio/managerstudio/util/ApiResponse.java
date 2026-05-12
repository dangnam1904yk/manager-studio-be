package com.manager.studio.managerstudio.util;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    private String message;
    private int code;
    private T data;

    public  static <T> ApiResponse<T> success(T data){
        return ApiResponse.<T>builder()
                .code(HttpStatus.OK.value())
                .message("success")
                .data(data)
                .build();
    }

    public  static <T> ApiResponse<T> success(T data, String message){
        return ApiResponse.<T>builder()
                .code(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .build();
    }

    public  static <T> ApiResponse<T> success(T data, String message, int code){
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    public  static <T> ApiResponse<T> bad(T data, String message){
        return ApiResponse.<T>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .data(data)
                .build();
    }

    public  static <T> ApiResponse<T> bad(T data, String message, int code){
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

}
