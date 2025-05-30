/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateDto(
		@Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters") String name,
		@Pattern(regexp = "^\\+[0-9]{7,15}$", message = "Phone number is invalid") String phone) {}
