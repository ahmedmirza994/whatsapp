/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Test Data Builder for ParticipantDto record - Industry Best Practice
 * Provides fluent API for creating test data with defaults and customization
 */
public class ParticipantDtoTestDataBuilder {

	private UUID id = UUID.randomUUID();
	private UUID userId = UUID.randomUUID();
	private String email = "participant@example.com";
	private String name = "Test Participant";
	private String profilePicture = "http://example.com/profile.jpg";
	private LocalDateTime joinedAt = LocalDateTime.now().minusDays(1);
	private LocalDateTime leftAt = null;
	private LocalDateTime lastReadAt = LocalDateTime.now();

	public static ParticipantDtoTestDataBuilder aParticipantDto() {
		return new ParticipantDtoTestDataBuilder();
	}

	public ParticipantDtoTestDataBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public ParticipantDtoTestDataBuilder withUserId(UUID userId) {
		this.userId = userId;
		return this;
	}

	public ParticipantDtoTestDataBuilder withEmail(String email) {
		this.email = email;
		return this;
	}

	public ParticipantDtoTestDataBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public ParticipantDtoTestDataBuilder withProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
		return this;
	}

	public ParticipantDtoTestDataBuilder withJoinedAt(LocalDateTime joinedAt) {
		this.joinedAt = joinedAt;
		return this;
	}

	public ParticipantDtoTestDataBuilder withLeftAt(LocalDateTime leftAt) {
		this.leftAt = leftAt;
		return this;
	}

	public ParticipantDtoTestDataBuilder withLastReadAt(LocalDateTime lastReadAt) {
		this.lastReadAt = lastReadAt;
		return this;
	}

	public ParticipantDtoTestDataBuilder withNullValues() {
		this.id = null;
		this.userId = null;
		this.email = null;
		this.name = null;
		this.profilePicture = null;
		this.joinedAt = null;
		this.leftAt = null;
		this.lastReadAt = null;
		return this;
	}

	public ParticipantDtoTestDataBuilder asActiveParticipant() {
		this.leftAt = null;
		this.lastReadAt = LocalDateTime.now();
		return this;
	}

	public ParticipantDtoTestDataBuilder asLeftParticipant() {
		this.leftAt = LocalDateTime.now();
		return this;
	}

	public ParticipantDto build() {
		return new ParticipantDto(
				id, userId, email, name, profilePicture, joinedAt, leftAt, lastReadAt);
	}
}

// Usage Example:
// ParticipantDto dto = aParticipantDto().withName("John
// Doe").withEmail("john@example.com").build();
// ParticipantDto activeParticipant = aParticipantDto().asActiveParticipant().build();
