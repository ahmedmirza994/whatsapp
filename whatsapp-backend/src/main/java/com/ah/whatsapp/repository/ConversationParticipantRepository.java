package com.ah.whatsapp.repository;

import com.ah.whatsapp.model.ConversationParticipant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
	List<ConversationParticipant> findByConversationId(UUID conversationId);

	/**
	 * Find all conversations that a user participates in
	 *
	 * @param userId the user ID
	 * @return list of conversation participants
	 */
	List<ConversationParticipant> findByUserId(UUID userId);

	/**
	 * Find a specific participant by conversation and user IDs
	 *
	 * @param conversationId the conversation ID
	 * @param userId the user ID
	 * @return optional containing the participant if found
	 */
	Optional<ConversationParticipant> findByConversationIdAndUserId(UUID conversationId, UUID userId);

	/**
	 * Check if a user is a participant in a conversation
	 *
	 * @param conversationId the conversation ID
	 * @param userId the user ID
	 * @return true if the user is a participant, false otherwise
	 */
	boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

	/**
	 * Remove a participant from a conversation
	 *
	 * @param id the participant ID
	 */
	void delete(UUID id);
}
