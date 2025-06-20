/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Conversation {
	private UUID id;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<ConversationParticipant> participants = new ArrayList<>();
	private List<Message> messages = new ArrayList<>();
	private Message lastMessage;
}
