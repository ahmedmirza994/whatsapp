/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ah.whatsapp.entity.ConversationEntity;

/**
 * Test Data Builder for ConversationEntity objects - Industry Best Practice
 * Provides fluent API for creating test data with defaults and customization
 */
public class ConversationEntityTestDataBuilder {

    private UUID id = UUID.randomUUID();
    private LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
    private LocalDateTime updatedAt = LocalDateTime.now();

    public static ConversationEntityTestDataBuilder aConversationEntity() {
        return new ConversationEntityTestDataBuilder();
    }

    public ConversationEntityTestDataBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public ConversationEntityTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ConversationEntityTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public ConversationEntity build() {
        ConversationEntity entity = new ConversationEntity();
        entity.setId(id);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        return entity;
    }
}
