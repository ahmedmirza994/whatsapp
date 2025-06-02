/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

import java.util.UUID;

/**
 * Test Data Builder for SendMessageRequest - Industry Best Practice
 * Provides fluent API for creating test data with defaults and customization
 */
public class SendMessageRequestTestDataBuilder {
	private UUID conversationId = UUID.randomUUID();
	private String content = "Test message content";

	public static SendMessageRequestTestDataBuilder aSendMessageRequest() {
		return new SendMessageRequestTestDataBuilder();
	}

	public SendMessageRequestTestDataBuilder withConversationId(UUID conversationId) {
		this.conversationId = conversationId;
		return this;
	}

	public SendMessageRequestTestDataBuilder withContent(String content) {
		this.content = content;
		return this;
	}

	public SendMessageRequest build() {
		return new SendMessageRequest(conversationId, content);
	}
}
