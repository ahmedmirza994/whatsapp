package com.ah.whatsapp.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipantDto(
	UUID id,
	UUID userId,
	String name,
	String profilePictureUrl,
	LocalDateTime joinedAt
) {

}
