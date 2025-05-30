/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Test Data Builder for MessageDto record - Industry Best Practice
 * Provides fluent API for creating test data with defaults and customization
 */
public class MessageDtoTestDataBuilder {

	private UUID id = UUID.randomUUID();
	private UUID conversationId = UUID.randomUUID();
	private UUID senderId = UUID.randomUUID();
	private String senderName = "Test Sender";
	private String content = "Hello, this is a test message";
	private LocalDateTime sentAt = LocalDateTime.now();

	public static MessageDtoTestDataBuilder aMessageDto() {
		return new MessageDtoTestDataBuilder();
	}

	public MessageDtoTestDataBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public MessageDtoTestDataBuilder withConversationId(UUID conversationId) {
		this.conversationId = conversationId;
		return this;
	}

	public MessageDtoTestDataBuilder withSenderId(UUID senderId) {
		this.senderId = senderId;
		return this;
	}

	public MessageDtoTestDataBuilder withSenderName(String senderName) {
		this.senderName = senderName;
		return this;
	}

	public MessageDtoTestDataBuilder withContent(String content) {
		this.content = content;
		return this;
	}

	public MessageDtoTestDataBuilder withSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
		return this;
	}

	public MessageDtoTestDataBuilder withNullValues() {
		this.id = null;
		this.conversationId = null;
		this.senderId = null;
		this.senderName = null;
		this.content = null;
		this.sentAt = null;
		return this;
	}

	public MessageDto build() {
		return new MessageDto(id, conversationId, senderId, senderName, content, sentAt);
	}
}

// Usage Example:
// MessageDto dto = aMessageDto().withContent("Hello World").withSenderName("John").build();
// MessageDto messageWithNulls = aMessageDto().withNullValues().build();
