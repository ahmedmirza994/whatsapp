/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

import java.util.UUID;

public record UserDto(
			UUID id,
			String name,
			String email,
			String phone,
			String profilePicture,
			String jwtToken
	) {}
