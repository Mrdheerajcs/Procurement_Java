package com.procurement.util;

import com.procurement.dto.responce.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", message, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return success(data, "Operation completed successfully");
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>("ERROR", message, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(String message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return error(HttpStatus.NOT_FOUND, message);
    }

    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return error(HttpStatus.UNAUTHORIZED, message);
    }

    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return error(HttpStatus.FORBIDDEN, message);
    }
}