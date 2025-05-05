package com.ah.whatsapp.service;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import com.ah.whatsapp.dto.ConversationDto;
import com.ah.whatsapp.dto.CreateConversationRequest;
import com.ah.whatsapp.exception.ConversationNotFoundException;

public interface ConversationService {

	/**
     * Creates a new conversation between users
     *
     * @param request conversation creation details
     * @param creatorId ID of the user creating the conversation
     * @return the created conversation
     */
    ConversationDto createConversation(CreateConversationRequest request, UUID creatorId);

    /**
     * Adds a participant to an existing conversation
     *
     * @param conversationId ID of the conversation
     * @param userId ID of the user to add
     */
    void addParticipant(UUID conversationId, UUID userId);

    /**
     * Gets all conversations for a specific user
     *
     * @param userId ID of the user
     * @return list of conversations the user is part of
     */
    List<ConversationDto> findUserConversations(UUID userId);

	/**
     * Finds a specific conversation by its ID, ensuring the requesting user is a participant.
     *
     * @param conversationId The ID of the conversation to find.
     * @param userId The ID of the user requesting the conversation.
     * @return The ConversationDto if found and user is a participant.
     * @throws ConversationNotFoundException if the conversation does not exist.
     * @throws AccessDeniedException if the user is not a participant in the conversation.
     */
    ConversationDto findConversationByIdAndUser(UUID conversationId, UUID userId)
            throws ConversationNotFoundException, AccessDeniedException;


	ConversationDto findOrCreateConversation(CreateConversationRequest createConversationRequest, UUID creatorId);
}
