/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(
		name = "conversation_participants",
		uniqueConstraints =
				@UniqueConstraint(
						columnNames = {"conversation_id", "user_id"},
						name = "uk_conversation_participant"))
public class ConversationParticipantEntity {
	@Id @GeneratedValue private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "conversation_id", nullable = false)
	private ConversationEntity conversation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Column(name = "joined_at", nullable = false)
	private LocalDateTime joinedAt;

	private boolean isActive;

	private LocalDateTime leftAt;
	private LocalDateTime lastReadAt;
}
