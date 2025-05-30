/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDto(
		@NotBlank(message = "Email is mandatory") @Email(message = "Email should be valid") String email,
		@NotBlank(message = "Password is mandatory") String password) {}
