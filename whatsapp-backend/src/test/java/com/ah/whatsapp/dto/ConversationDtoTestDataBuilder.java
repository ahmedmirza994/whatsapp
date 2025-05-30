/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Test Data Builder for ConversationDto class - Industry Best Practice
 * Provides fluent API for creating test data with defaults and customization
 */
public class ConversationDtoTestDataBuilder {

	private UUID id = UUID.randomUUID();
	private LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
	private LocalDateTime updatedAt = LocalDateTime.now();
	private List<ParticipantDto> participants = new ArrayList<>();
	private List<MessageDto> messages = new ArrayList<>();
	private MessageDto lastMessage = null;

	public static ConversationDtoTestDataBuilder aConversationDto() {
		return new ConversationDtoTestDataBuilder();
	}

	public ConversationDtoTestDataBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public ConversationDtoTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
		return this;
	}

	public ConversationDtoTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
		return this;
	}

	public ConversationDtoTestDataBuilder withParticipants(List<ParticipantDto> participants) {
		this.participants =
				participants != null ? new ArrayList<>(participants) : new ArrayList<>();
		return this;
	}

	public ConversationDtoTestDataBuilder withParticipant(ParticipantDto participant) {
		if (this.participants == null) {
			this.participants = new ArrayList<>();
		}
		this.participants.add(participant);
		return this;
	}

	public ConversationDtoTestDataBuilder withMessages(List<MessageDto> messages) {
		this.messages = messages != null ? new ArrayList<>(messages) : new ArrayList<>();
		return this;
	}

	public ConversationDtoTestDataBuilder withMessage(MessageDto message) {
		if (this.messages == null) {
			this.messages = new ArrayList<>();
		}
		this.messages.add(message);
		return this;
	}

	public ConversationDtoTestDataBuilder withLastMessage(MessageDto lastMessage) {
		this.lastMessage = lastMessage;
		return this;
	}

	public ConversationDtoTestDataBuilder withNullValues() {
		this.id = null;
		this.createdAt = null;
		this.updatedAt = null;
		this.participants = null;
		this.messages = null;
		this.lastMessage = null;
		return this;
	}

	public ConversationDtoTestDataBuilder withEmptyLists() {
		this.participants = new ArrayList<>();
		this.messages = new ArrayList<>();
		this.lastMessage = null;
		return this;
	}

	public ConversationDto build() {
		ConversationDto dto = new ConversationDto();
		dto.setId(id);
		dto.setCreatedAt(createdAt);
		dto.setUpdatedAt(updatedAt);
		dto.setParticipants(participants);
		dto.setMessages(messages);
		dto.setLastMessage(lastMessage);
		return dto;
	}
}

// Usage Example:
// ConversationDto dto =
// aConversationDto().withParticipant(participant).withMessage(message).build();
// ConversationDto emptyDto = aConversationDto().withEmptyLists().build();
