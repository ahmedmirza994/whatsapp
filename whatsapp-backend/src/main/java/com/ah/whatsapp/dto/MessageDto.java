package com.ah.whatsapp.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageDto(
	UUID id,
	UUID conversationId,
	UUID senderId,
	String senderName,
	String content,
	LocalDateTime sentAt
) {

}
