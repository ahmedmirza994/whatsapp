/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConversationParticipant {
	private UUID id;
	private UUID conversationId;
	private UUID participantId;
	private String participantEmail;
	private String participantName;
	private String participantProfilePicture;
	private LocalDateTime joinedAt;
	private boolean isActive;
	private LocalDateTime leftAt;
	private LocalDateTime lastReadAt;
}
