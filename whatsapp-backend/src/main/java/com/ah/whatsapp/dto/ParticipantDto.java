/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipantDto(
		UUID id,
		UUID userId,
		String email,
		String name,
		String profilePicture,
		LocalDateTime joinedAt,
		LocalDateTime leftAt,
		LocalDateTime lastReadAt) {}
