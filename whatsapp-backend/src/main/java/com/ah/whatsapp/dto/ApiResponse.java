/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ApiResponse<T> {
    private Integer status;
    private String error;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), null, data);
    }

    public static <T> ApiResponse<T> failure(String error, HttpStatus status) {
        return new ApiResponse<>(status.value(), error, null);
    }

    public static <T> ApiResponse<T> badRequest(String error) {
        return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), error, null);
    }

    public static <T> ApiResponse<T> unauthorized(String error) {
        return new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), error, null);
    }
}
