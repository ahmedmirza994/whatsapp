/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.entity.MessageEntity;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.model.Message;
import com.ah.whatsapp.model.User;

/**
 * Test Data Builder for Message objects - Industry Best Practice
 * Provides fluent API for creating test data with defaults and customization
 */
public class MessageTestDataBuilder {

    private UUID id = UUID.randomUUID();
    private UUID conversationId = UUID.randomUUID();
    private User sender = UserTestDataBuilder.aUser().build();
    private String content = "Test message content";
    private LocalDateTime sentAt = LocalDateTime.now();

    // For entity building
    private ConversationEntity conversationEntity;
    private UserEntity senderEntity;

    public static MessageTestDataBuilder aMessage() {
        return new MessageTestDataBuilder();
    }

    public MessageTestDataBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public MessageTestDataBuilder withConversationId(UUID conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public MessageTestDataBuilder withSender(User sender) {
        this.sender = sender;
        return this;
    }

    public MessageTestDataBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public MessageTestDataBuilder withSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
        return this;
    }

    public MessageTestDataBuilder withConversationEntity(ConversationEntity conversationEntity) {
        this.conversationEntity = conversationEntity;
        return this;
    }

    public MessageTestDataBuilder withSenderEntity(UserEntity senderEntity) {
        this.senderEntity = senderEntity;
        return this;
    }

    public MessageTestDataBuilder withNullValues() {
        this.content = null;
        this.sender = null;
        return this;
    }

    public Message build() {
        Message message = new Message();
        message.setId(id);
        message.setConversationId(conversationId);
        message.setSender(sender);
        message.setContent(content);
        message.setSentAt(sentAt);
        return message;
    }

    public MessageEntity buildEntity() {
        MessageEntity entity = new MessageEntity();
        entity.setId(id);
        entity.setConversation(conversationEntity);
        entity.setSender(senderEntity);
        entity.setContent(content);
        entity.setSentAt(sentAt);
        return entity;
    }

    public MessageEntity buildEntity(
            ConversationEntity conversationEntity, UserEntity senderEntity) {
        MessageEntity entity = new MessageEntity();
        entity.setId(id);
        entity.setConversation(conversationEntity);
        entity.setSender(senderEntity);
        entity.setContent(content);
        entity.setSentAt(sentAt);
        return entity;
    }
}

// Usage Example:
// Message message = aMessage().withContent("Hello World").withSender(testUser).build();
// MessageEntity entity = aMessage().buildEntity(conversationEntity, userEntity);
