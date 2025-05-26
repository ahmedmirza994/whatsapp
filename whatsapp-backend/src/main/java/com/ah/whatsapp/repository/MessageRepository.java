/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.ah.whatsapp.model.Message;

public interface MessageRepository {
    Message save(Message message);

    Optional<Message> findById(UUID id);

    List<Message> findByConversationIdAndSentAtAfter(UUID conversationId, LocalDateTime sentAt);

    void delete(UUID id);

    Optional<Message> findLatestByConversationId(UUID conversationId);

    /**
     * Finds the latest message for each of the given conversation IDs.
     *
     * @param conversationIds A list of conversation IDs.
     * @return A Map where the key is the conversation ID and the value is the latest Message in that conversation.
     */
    Map<UUID, Message> findLatestMessagesForConversations(List<UUID> conversationIds);
}
