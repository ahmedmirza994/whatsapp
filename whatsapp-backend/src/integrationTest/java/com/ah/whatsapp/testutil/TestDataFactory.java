/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.testutil;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.entity.MessageEntity;
import com.ah.whatsapp.entity.UserEntity;

/**
 * Test data factory for creating test entities.
 * Provides static methods to create test data objects with sensible defaults.
 * Follows the Test Data Builder pattern for maintainable test data creation.
 */
public final class TestDataFactory {

	private TestDataFactory() {
		// Utility class - prevent instantiation
	}

	/**
	 * Creates a test user with default values.
	 *
	 * @return UserEntity with test data
	 */
	public static UserEntity createTestUser() {
		return createTestUser("John Doe", "john.doe@example.com", "+1234567890");
	}

	/**
	 * Creates a test user with specified values.
	 *
	 * @param name the user's name
	 * @param email the user's email
	 * @param phone the user's phone number
	 * @return UserEntity with specified data
	 */
	public static UserEntity createTestUser(String name, String email, String phone) {
		UserEntity user = new UserEntity();
		user.setName(name);
		user.setEmail(email);
		user.setPhone(phone);
		user.setPassword("encrypted_password_" + System.currentTimeMillis());
		user.setCreatedAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());
		return user;
	}

	/**
	 * Creates a test conversation.
	 *
	 * @return ConversationEntity with test data
	 */
	public static ConversationEntity createTestConversation() {
		ConversationEntity conversation = new ConversationEntity();
		conversation.setCreatedAt(LocalDateTime.now());
		conversation.setUpdatedAt(LocalDateTime.now());
		return conversation;
	}

	/**
	 * Creates a test message.
	 *
	 * @param conversation the conversation this message belongs to
	 * @param sender the user who sent the message
	 * @param content the message content
	 * @return MessageEntity with test data
	 */
	public static MessageEntity createTestMessage(
			ConversationEntity conversation, UserEntity sender, String content) {
		MessageEntity message = new MessageEntity();
		message.setConversation(conversation);
		message.setSender(sender);
		message.setContent(content);
		message.setSentAt(LocalDateTime.now());
		return message;
	}

	/**
	 * Creates a test message with default content.
	 *
	 * @param conversation the conversation this message belongs to
	 * @param sender the user who sent the message
	 * @return MessageEntity with test data
	 */
	public static MessageEntity createTestMessage(
			ConversationEntity conversation, UserEntity sender) {
		return createTestMessage(conversation, sender, "Test message content");
	}

	/**
	 * Creates a unique test email address.
	 *
	 * @param prefix the email prefix
	 * @return unique email address for testing
	 */
	public static String createUniqueEmail(String prefix) {
		return prefix + "_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
	}

	/**
	 * Creates a unique test phone number.
	 *
	 * @return unique phone number for testing
	 */
	public static String createUniquePhone() {
		return "+1" + (1000000000L + (long) (Math.random() * 8999999999L));
	}
}
