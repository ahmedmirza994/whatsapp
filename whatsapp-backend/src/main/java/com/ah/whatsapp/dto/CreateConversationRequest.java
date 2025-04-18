package com.ah.whatsapp.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateConversationRequest(
	@NotNull(message = "Participant ID is required")
	UUID participantId
) {

}
