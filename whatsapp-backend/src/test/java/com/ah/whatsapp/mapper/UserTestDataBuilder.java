/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.model.User;

/**
 * Test Data Builder for User objects - Industry Best Practice
 * Provides fluent API for creating test data with defaults and customization
 */
public class UserTestDataBuilder {

    private UUID id = UUID.randomUUID();
    private String name = "Test User";
    private String email = "test@example.com";
    private String password = "password123";
    private String phone = "+1234567890";
    private String profilePicture = "http://example.com/profile.jpg";
    private LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
    private LocalDateTime updatedAt = LocalDateTime.now();

    public static UserTestDataBuilder aUser() {
        return new UserTestDataBuilder();
    }

    public UserTestDataBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public UserTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public UserTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserTestDataBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public UserTestDataBuilder withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public UserTestDataBuilder withProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
        return this;
    }

    public UserTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public UserTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public UserTestDataBuilder withNullValues() {
        this.name = null;
        this.password = null;
        this.phone = null;
        this.profilePicture = null;
        return this;
    }

    public User build() {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhone(phone);
        user.setProfilePicture(profilePicture);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);
        return user;
    }

    public UserEntity buildEntity() {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setEmail(email);
        entity.setPassword(password);
        entity.setPhone(phone);
        entity.setProfilePicture(profilePicture);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        return entity;
    }
}

// Usage Example:
// User user = aUser().withName("John Doe").withEmail("john@example.com").build();
// UserEntity entity = aUser().withNullValues().buildEntity();
