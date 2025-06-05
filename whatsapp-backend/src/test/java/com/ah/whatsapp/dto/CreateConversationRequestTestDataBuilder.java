/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

import java.util.UUID;

public class CreateConversationRequestTestDataBuilder {

	private UUID participantId = UUID.randomUUID();

	public static CreateConversationRequestTestDataBuilder aCreateConversationRequest() {
		return new CreateConversationRequestTestDataBuilder();
	}

	public CreateConversationRequestTestDataBuilder withParticipantId(UUID participantId) {
		this.participantId = participantId;
		return this;
	}

	public CreateConversationRequest build() {
		return new CreateConversationRequest(participantId);
	}
}
