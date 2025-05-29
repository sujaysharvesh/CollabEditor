package com.example.CollabAuth.User;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ApiResponse<T> {

    private boolean success;
    private Integer statusCode;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private List<String> errors;

    public static <T> ApiResponse<T> success(String message, T data, Integer statusCode) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(statusCode)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String error, Integer statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(error)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

}
