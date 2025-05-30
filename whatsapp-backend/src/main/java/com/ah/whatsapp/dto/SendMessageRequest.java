/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendMessageRequest(
		@NotNull(message = "Conversation ID is required") UUID conversationId,
		@NotBlank(message = "Message content cannot be empty") String content) {}
