package com.ah.whatsapp.dto;

import java.util.UUID;

public record DeleteMessageEvent(
	UUID messageId,
	UUID conversationId
) {
}
