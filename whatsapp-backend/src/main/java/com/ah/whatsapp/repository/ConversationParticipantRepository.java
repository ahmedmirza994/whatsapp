/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.ah.whatsapp.model.ConversationParticipant;

public interface ConversationParticipantRepository {
	/**
	 * Save a conversation participant
	 *
	 * @param participant the participant to save
	 * @return the saved participant
	 */
	ConversationParticipant save(ConversationParticipant participant);

	/**
	 * Find a conversation participant by ID
	 *
	 * @param id the participant ID
	 * @return optional containing the participant if found
	 */
	Optional<ConversationParticipant> findById(UUID id);

	/**
	 * Find all participants in a conversation
	 *
	 * @param conversationId the conversation ID
	 * @return list of participants in the conversation
	 */
	List<ConversationParticipant> findByConversationIdAndIsActiveTrue(UUID conversationId);

	/**
	 * Check if a user is a participant in a conversation
	 *
	 * @param conversationId the conversation ID
	 * @param userId         the user ID
	 * @return true if the user is a participant, false otherwise
	 */
	boolean existsByConversationIdAndUserIdAndIsActiveTrue(UUID conversationId, UUID userId);

	/**
	 * Finds all participants for a list of conversation IDs.
	 *
	 * @param conversationIds The list of conversation IDs.
	 * @return A Map where the key is the conversation ID and the value is a list of participants in that conversation.
	 */
	Map<UUID, List<ConversationParticipant>> findParticipantsForConversationsAndIsActiveTrue(
			List<UUID> conversationIds);

	/**
	 * Find a active conversation participant by conversation ID and user ID
	 * @param conversationId
	 * @param userId
	 * @return ConversationParticipant
	 */
	Optional<ConversationParticipant> findByConversationIdAndUserIdAndIsActiveTrue(
			UUID conversationId, UUID userId);

	/**
	 * Find all participants in a conversation
	 *
	 * @param conversationId the conversation ID
	 * @return list of participants in the conversation
	 */
	List<ConversationParticipant> findByConversationId(UUID conversationId);

	/**
	 * Find a conversation participant by conversation ID and user ID
	 * @param conversationId
	 * @param userId
	 * @return ConversationParticipant
	 */
	Optional<ConversationParticipant> findByConversationIdAndUserId(
			UUID conversationId, UUID userId);
}
