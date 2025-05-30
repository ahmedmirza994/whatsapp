/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ah.whatsapp.dto.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExistsException(
			UserAlreadyExistsException ex) {
		ApiResponse<Void> response = ApiResponse.failure(ex.getMessage(), HttpStatus.CONFLICT);
		return new ResponseEntity<>(response, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<List<String>>> handleValidationException(
			MethodArgumentNotValidException ex) {
		String errors =
				ex.getBindingResult().getAllErrors().stream()
						.map(DefaultMessageSourceResolvable::getDefaultMessage)
						.collect(Collectors.joining(","));

		ApiResponse<List<String>> response = ApiResponse.badRequest(errors);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidCredentialsException(
			InvalidCredentialsException ex) {
		ApiResponse<Void> response = ApiResponse.failure(ex.getMessage(), HttpStatus.UNAUTHORIZED);
		return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleUsernameNotFoundException(
			UsernameNotFoundException ex) {
		ApiResponse<Void> response = ApiResponse.failure(ex.getMessage(), HttpStatus.UNAUTHORIZED);
		return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException ex) {
		ApiResponse<Void> response = ApiResponse.failure(ex.getMessage(), HttpStatus.NOT_FOUND);
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(UnauthorizedException ex) {
		ApiResponse<Void> response = ApiResponse.failure(ex.getMessage(), HttpStatus.UNAUTHORIZED);
		return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
		ApiResponse<Void> response = ApiResponse.failure(ex.getMessage(), HttpStatus.UNAUTHORIZED);
		return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
	}
}
