package com.ah.whatsapp.service;

import java.util.List;
import java.util.UUID;

import com.ah.whatsapp.dto.MessageDto;
import com.ah.whatsapp.dto.SendMessageRequest;

public interface MessageService {
	/**
     * Sends a new message in a conversation
     *
     * @param request message details
     * @param senderId ID of the user sending the message
     * @return the created message
     */
    MessageDto sendMessage(SendMessageRequest request, UUID senderId);

    /**
     * Retrieves all messages for a specific conversation
     *
     * @param conversationId ID of the conversation
     * @return list of messages in the conversation
     */
    List<MessageDto> findConversationMessages(UUID conversationId);
}
