package com.ah.whatsapp.service;

import java.util.List;
import java.util.UUID;

import com.ah.whatsapp.dto.ConversationDto;
import com.ah.whatsapp.dto.CreateConversationRequest;

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

}
