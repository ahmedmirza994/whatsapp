/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ah.whatsapp.model.Conversation;

public interface ConversationRepository {
	Conversation save(Conversation conversation);

	Optional<Conversation> findById(UUID id);

	List<Conversation> findByUserId(UUID userId);

	void delete(UUID id);

	boolean existsById(UUID id);

	Optional<Conversation> findDirectConversationBetweenUsers(UUID userId1, UUID userId2);
}
