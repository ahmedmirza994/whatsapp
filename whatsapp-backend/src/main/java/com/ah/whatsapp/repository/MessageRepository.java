package com.ah.whatsapp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ah.whatsapp.model.Message;

public interface MessageRepository {
	Message save(Message message);
    Optional<Message> findById(UUID id);
    List<Message> findByConversationId(UUID conversationId);
    void delete(UUID id);
}
