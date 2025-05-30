/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.entity.ConversationParticipantEntity;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.model.ConversationParticipant;

/**
 * Test Data Builder for ConversationParticipant objects - Industry Best Practice
 * Provides fluent API for creating test data with defaults and customization
 */
public class ConversationParticipantTestDataBuilder {

	private UUID id = UUID.randomUUID();
	private UUID conversationId = UUID.randomUUID();
	private UUID participantId = UUID.randomUUID();
	private String participantName = "Test User";
	private String participantEmail = "test@example.com";
	private String participantProfilePicture = "http://example.com/profile.jpg";
	private LocalDateTime joinedAt = LocalDateTime.now().minusDays(1);
	private boolean active = true;
	private LocalDateTime leftAt = null;
	private LocalDateTime lastReadAt = LocalDateTime.now().minusHours(1);

	public static ConversationParticipantTestDataBuilder aConversationParticipant() {
		return new ConversationParticipantTestDataBuilder();
	}

	public ConversationParticipantTestDataBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public ConversationParticipantTestDataBuilder withConversationId(UUID conversationId) {
		this.conversationId = conversationId;
		return this;
	}

	public ConversationParticipantTestDataBuilder withParticipantId(UUID participantId) {
		this.participantId = participantId;
		return this;
	}

	public ConversationParticipantTestDataBuilder withParticipantName(String participantName) {
		this.participantName = participantName;
		return this;
	}

	public ConversationParticipantTestDataBuilder withParticipantEmail(String participantEmail) {
		this.participantEmail = participantEmail;
		return this;
	}

	public ConversationParticipantTestDataBuilder withParticipantProfilePicture(
			String participantProfilePicture) {
		this.participantProfilePicture = participantProfilePicture;
		return this;
	}

	public ConversationParticipantTestDataBuilder withJoinedAt(LocalDateTime joinedAt) {
		this.joinedAt = joinedAt;
		return this;
	}

	public ConversationParticipantTestDataBuilder withActive(boolean active) {
		this.active = active;
		return this;
	}

	public ConversationParticipantTestDataBuilder withLeftAt(LocalDateTime leftAt) {
		this.leftAt = leftAt;
		return this;
	}

	public ConversationParticipantTestDataBuilder withLastReadAt(LocalDateTime lastReadAt) {
		this.lastReadAt = lastReadAt;
		return this;
	}

	public ConversationParticipantTestDataBuilder withInactiveState() {
		this.active = false;
		this.leftAt = LocalDateTime.now();
		return this;
	}

	public ConversationParticipantTestDataBuilder withNullDates() {
		this.joinedAt = null;
		this.leftAt = null;
		this.lastReadAt = null;
		return this;
	}

	public ConversationParticipant build() {
		ConversationParticipant participant = new ConversationParticipant();
		participant.setId(id);
		participant.setConversationId(conversationId);
		participant.setParticipantId(participantId);
		participant.setParticipantName(participantName);
		participant.setParticipantEmail(participantEmail);
		participant.setParticipantProfilePicture(participantProfilePicture);
		participant.setJoinedAt(joinedAt);
		participant.setActive(active);
		participant.setLeftAt(leftAt);
		participant.setLastReadAt(lastReadAt);
		return participant;
	}

	public ConversationParticipantEntity buildEntity(
			ConversationEntity conversationEntity, UserEntity userEntity) {
		ConversationParticipantEntity entity = new ConversationParticipantEntity();
		entity.setId(id);
		entity.setConversation(conversationEntity);
		entity.setUser(userEntity);
		entity.setJoinedAt(joinedAt);
		entity.setActive(active);
		entity.setLeftAt(leftAt);
		entity.setLastReadAt(lastReadAt);
		return entity;
	}
}
