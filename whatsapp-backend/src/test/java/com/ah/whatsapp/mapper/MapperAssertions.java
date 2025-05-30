/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.mapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import com.ah.whatsapp.entity.ConversationParticipantEntity;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.model.User;

/**
 * Custom Assertion Utilities for Mapper Tests - Industry Best Practice
 * Provides domain-specific assertions for better test readability
 */
public class MapperAssertions {

	/**
	 * Asserts that a User model and UserEntity have matching field values
	 */
	public static void assertUserModelMatchesEntity(User user, UserEntity entity) {
		assertAll(
				"User model should match entity fields",
				() -> assertEquals(user.getId(), entity.getId(), "ID should match"),
				() -> assertEquals(user.getName(), entity.getName(), "Name should match"),
				() -> assertEquals(user.getEmail(), entity.getEmail(), "Email should match"),
				() ->
						assertEquals(
								user.getPassword(), entity.getPassword(), "Password should match"),
				() -> assertEquals(user.getPhone(), entity.getPhone(), "Phone should match"),
				() ->
						assertEquals(
								user.getProfilePicture(),
								entity.getProfilePicture(),
								"Profile picture should match"),
				() ->
						assertEquals(
								user.getCreatedAt(),
								entity.getCreatedAt(),
								"Created date should match"),
				() ->
						assertEquals(
								user.getUpdatedAt(),
								entity.getUpdatedAt(),
								"Updated date should match"));
	}

	/**
	 * Asserts that a UserEntity and User model have matching field values
	 */
	public static void assertEntityMatchesUserModel(UserEntity entity, User user) {
		assertUserModelMatchesEntity(user, entity);
	}

	/**
	 * Asserts that two User objects are equivalent
	 */
	public static void assertUsersAreEquivalent(User expected, User actual) {
		assertAll(
				"Users should be equivalent",
				() -> assertEquals(expected.getId(), actual.getId(), "ID should match"),
				() -> assertEquals(expected.getName(), actual.getName(), "Name should match"),
				() -> assertEquals(expected.getEmail(), actual.getEmail(), "Email should match"),
				() ->
						assertEquals(
								expected.getPassword(),
								actual.getPassword(),
								"Password should match"),
				() -> assertEquals(expected.getPhone(), actual.getPhone(), "Phone should match"),
				() ->
						assertEquals(
								expected.getProfilePicture(),
								actual.getProfilePicture(),
								"Profile picture should match"),
				() ->
						assertEquals(
								expected.getCreatedAt(),
								actual.getCreatedAt(),
								"Created date should match"),
				() ->
						assertEquals(
								expected.getUpdatedAt(),
								actual.getUpdatedAt(),
								"Updated date should match"));
	}

	/**
	 * Asserts that specific fields are null in a User object
	 */
	public static void assertUserHasNullFields(User user, Consumer<User> nullFieldsChecker) {
		nullFieldsChecker.accept(user);
	}

	/**
	 * Asserts that timestamp fields are properly set and reasonable
	 */
	public static void assertTimestampsAreValid(User user) {
		assertAll(
				"Timestamps should be valid",
				() -> assertNotNull(user.getCreatedAt(), "Created date should not be null"),
				() -> assertNotNull(user.getUpdatedAt(), "Updated date should not be null"),
				() ->
						assertTrue(
								user.getCreatedAt().isBefore(user.getUpdatedAt().plusSeconds(1)),
								"Created date should be before or equal to updated date"));
	}

	/**
	 * Asserts that ConversationParticipant model and ConversationParticipantEntity have matching field values
	 */
	public static void assertConversationParticipantModelMatchesEntity(
			ConversationParticipant model, ConversationParticipantEntity entity) {
		assertAll(
				"ConversationParticipant model should match entity fields",
				() -> assertEquals(model.getId(), entity.getId(), "ID should match"),
				() ->
						assertEquals(
								model.getConversationId(),
								entity.getConversation().getId(),
								"Conversation ID should match"),
				() ->
						assertEquals(
								model.getParticipantId(),
								entity.getUser().getId(),
								"Participant ID should match"),
				() ->
						assertEquals(
								model.getParticipantName(),
								entity.getUser().getName(),
								"Participant name should match"),
				() ->
						assertEquals(
								model.getParticipantEmail(),
								entity.getUser().getEmail(),
								"Participant email should match"),
				() ->
						assertEquals(
								model.getParticipantProfilePicture(),
								entity.getUser().getProfilePicture(),
								"Participant profile picture should match"),
				() ->
						assertEquals(
								model.getJoinedAt(),
								entity.getJoinedAt(),
								"Joined date should match"),
				() ->
						assertEquals(
								model.isActive(), entity.isActive(), "Active status should match"),
				() -> assertEquals(model.getLeftAt(), entity.getLeftAt(), "Left date should match"),
				() ->
						assertEquals(
								model.getLastReadAt(),
								entity.getLastReadAt(),
								"Last read date should match"));
	}

	/**
	 * Asserts that ConversationParticipantEntity and ConversationParticipant model have matching field values
	 */
	public static void assertConversationParticipantEntityMatchesModel(
			ConversationParticipantEntity entity, ConversationParticipant model) {
		assertConversationParticipantModelMatchesEntity(model, entity);
	}

	/**
	 * Asserts that two ConversationParticipant objects are equivalent
	 */
	public static void assertConversationParticipantsAreEquivalent(
			ConversationParticipant expected, ConversationParticipant actual) {
		assertAll(
				"ConversationParticipants should be equivalent",
				() -> assertEquals(expected.getId(), actual.getId(), "ID should match"),
				() ->
						assertEquals(
								expected.getConversationId(),
								actual.getConversationId(),
								"Conversation ID should match"),
				() ->
						assertEquals(
								expected.getParticipantId(),
								actual.getParticipantId(),
								"Participant ID should match"),
				() ->
						assertEquals(
								expected.getParticipantName(),
								actual.getParticipantName(),
								"Participant name should match"),
				() ->
						assertEquals(
								expected.getParticipantEmail(),
								actual.getParticipantEmail(),
								"Participant email should match"),
				() ->
						assertEquals(
								expected.getParticipantProfilePicture(),
								actual.getParticipantProfilePicture(),
								"Participant profile picture should match"),
				() ->
						assertEquals(
								expected.getJoinedAt(),
								actual.getJoinedAt(),
								"Joined date should match"),
				() ->
						assertEquals(
								expected.isActive(),
								actual.isActive(),
								"Active status should match"),
				() ->
						assertEquals(
								expected.getLeftAt(), actual.getLeftAt(), "Left date should match"),
				() ->
						assertEquals(
								expected.getLastReadAt(),
								actual.getLastReadAt(),
								"Last read date should match"));
	}
}

// Usage Example:
// assertUserModelMatchesEntity(user, entity);
// assertUsersAreEquivalent(expectedUser, actualUser);
