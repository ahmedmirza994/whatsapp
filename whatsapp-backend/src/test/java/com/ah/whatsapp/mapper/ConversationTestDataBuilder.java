/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.model.Conversation;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.model.Message;

/**
 * Test Data Builder for Conversation objects - Industry Best Practice
 * Provides fluent API for creating test data with defaults and customization
 */
public class ConversationTestDataBuilder {

    private UUID id = UUID.randomUUID();
    private LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
    private LocalDateTime updatedAt = LocalDateTime.now();
    private List<ConversationParticipant> participants = new ArrayList<>();
    private List<Message> messages = new ArrayList<>();
    private Message lastMessage = null;

    public static ConversationTestDataBuilder aConversation() {
        return new ConversationTestDataBuilder();
    }

    public ConversationTestDataBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public ConversationTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ConversationTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public ConversationTestDataBuilder withParticipants(
            List<ConversationParticipant> participants) {
        this.participants = participants;
        return this;
    }

    public ConversationTestDataBuilder withMessages(List<Message> messages) {
        this.messages = messages;
        return this;
    }

    public ConversationTestDataBuilder withLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
        return this;
    }

    public ConversationTestDataBuilder withNullValues() {
        this.participants = null;
        this.messages = null;
        this.lastMessage = null;
        return this;
    }

    public ConversationTestDataBuilder withEmptyCollections() {
        this.participants = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.lastMessage = null;
        return this;
    }

    public Conversation build() {
        Conversation conversation = new Conversation();
        conversation.setId(id);
        conversation.setCreatedAt(createdAt);
        conversation.setUpdatedAt(updatedAt);
        conversation.setParticipants(participants);
        conversation.setMessages(messages);
        conversation.setLastMessage(lastMessage);
        return conversation;
    }

    public ConversationEntity buildEntity() {
        ConversationEntity entity = new ConversationEntity();
        entity.setId(id);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        return entity;
    }
}

// Usage Example:
// Conversation conversation = aConversation().withParticipants(participantList).build();
// ConversationEntity entity = aConversation().buildEntity();
