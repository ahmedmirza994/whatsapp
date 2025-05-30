/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
	@EqualsAndHashCode.Include private UUID id;

	private String name;
	private String email;
	private String password;
	private String phone;
	private String profilePicture;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
