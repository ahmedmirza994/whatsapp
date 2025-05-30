/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

import java.util.UUID;

import com.ah.whatsapp.enums.EventType;

/**
 * Test Data Builder for TypingIndicatorDto class - Industry Best Practice
 * Provides fluent API for creating test data with defaults and customization
 */
public class TypingIndicatorDtoTestDataBuilder {

	private UUID conversationId = UUID.randomUUID();
	private UUID userId = UUID.randomUUID();
	private EventType eventType = EventType.TYPING_START;

	public static TypingIndicatorDtoTestDataBuilder aTypingIndicatorDto() {
		return new TypingIndicatorDtoTestDataBuilder();
	}

	public TypingIndicatorDtoTestDataBuilder withConversationId(UUID conversationId) {
		this.conversationId = conversationId;
		return this;
	}

	public TypingIndicatorDtoTestDataBuilder withUserId(UUID userId) {
		this.userId = userId;
		return this;
	}

	public TypingIndicatorDtoTestDataBuilder withEventType(EventType eventType) {
		this.eventType = eventType;
		return this;
	}

	public TypingIndicatorDtoTestDataBuilder withTypingStart() {
		this.eventType = EventType.TYPING_START;
		return this;
	}

	public TypingIndicatorDtoTestDataBuilder withTypingStop() {
		this.eventType = EventType.TYPING_STOP;
		return this;
	}

	public TypingIndicatorDtoTestDataBuilder withNullValues() {
		this.conversationId = null;
		this.userId = null;
		this.eventType = null;
		return this;
	}

	public TypingIndicatorDto build() {
		return new TypingIndicatorDto(conversationId, userId, eventType);
	}
}

// Usage Example:
// TypingIndicatorDto dto = aTypingIndicatorDto().withTypingStart().build();
// TypingIndicatorDto stopDto = aTypingIndicatorDto().withTypingStop().build();
