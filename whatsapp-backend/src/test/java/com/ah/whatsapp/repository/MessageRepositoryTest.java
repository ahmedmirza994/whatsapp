/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ah.whatsapp.entity.ConversationEntity;
import com.ah.whatsapp.entity.MessageEntity;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.exception.ConversationNotFoundException;
import com.ah.whatsapp.exception.UserNotFoundException;
import com.ah.whatsapp.mapper.MessageMapper;
import com.ah.whatsapp.mapper.MessageTestDataBuilder;
import com.ah.whatsapp.mapper.UserTestDataBuilder;
import com.ah.whatsapp.model.Message;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.entity.ConversationEntityRepository;
import com.ah.whatsapp.repository.entity.MessageEntityRepository;
import com.ah.whatsapp.repository.entity.UserEntityRepository;
import com.ah.whatsapp.repository.impl.MessageRepositoryImpl;

/**
 * Comprehensive Unit Tests for MessageRepository
 * Following industry best practices for repository layer testing
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessageRepository Tests")
class MessageRepositoryTest {

	@Mock private MessageEntityRepository messageEntityRepository;

	@Mock private ConversationEntityRepository conversationEntityRepository;

	@Mock private UserEntityRepository userEntityRepository;

	@Mock private MessageMapper messageMapper;

	@InjectMocks private MessageRepositoryImpl messageRepository;

	private MessageTestDataBuilder messageTestDataBuilder;
	private UserTestDataBuilder userTestDataBuilder;
	private Message testMessage;
	private MessageEntity testMessageEntity;
	private ConversationEntity testConversationEntity;
	private UserEntity testUserEntity;
	private User testUser;
	private UUID testMessageId;
	private UUID testConversationId;
	private UUID testUserId;

	@BeforeEach
	void setUp() {
		messageTestDataBuilder = new MessageTestDataBuilder();
		userTestDataBuilder = new UserTestDataBuilder();

		testMessageId = UUID.randomUUID();
		testConversationId = UUID.randomUUID();
		testUserId = UUID.randomUUID();

		// Using test data builders for consistent test data
		testUser =
				userTestDataBuilder
						.withId(testUserId)
						.withName("John Doe")
						.withEmail("john.doe@example.com")
						.build();

		testMessage =
				messageTestDataBuilder
						.withId(testMessageId)
						.withConversationId(testConversationId)
						.withSender(testUser)
						.withContent("Test message content")
						.build();

		testMessageEntity = new MessageEntity();
		testMessageEntity.setId(testMessageId);
		testMessageEntity.setContent("Test message content");
		testMessageEntity.setSentAt(LocalDateTime.now());

		testConversationEntity = new ConversationEntity();
		testConversationEntity.setId(testConversationId);

		testUserEntity = new UserEntity();
		testUserEntity.setId(testUserId);
		testUserEntity.setName("John Doe");
		testUserEntity.setEmail("john.doe@example.com");
	}

	@Nested
	@DisplayName("Save Message Tests")
	class SaveMessageTests {

		@Test
		@DisplayName("Should save message successfully when conversation and user exist")
		void save_ShouldReturnSavedMessage_WhenValidMessageProvided() {
			// Given
			when(conversationEntityRepository.findById(testConversationId))
					.thenReturn(Optional.of(testConversationEntity));
			when(userEntityRepository.findById(testUserId)).thenReturn(Optional.of(testUserEntity));
			when(messageMapper.toEntity(testMessage, testConversationEntity, testUserEntity))
					.thenReturn(testMessageEntity);
			when(messageEntityRepository.save(testMessageEntity)).thenReturn(testMessageEntity);
			when(messageMapper.toModel(testMessageEntity)).thenReturn(testMessage);

			// When
			Message savedMessage = messageRepository.save(testMessage);

			// Then
			assertNotNull(savedMessage);
			assertEquals(testMessage.getId(), savedMessage.getId());
			assertEquals(testMessage.getContent(), savedMessage.getContent());

			// Verify interactions
			verify(conversationEntityRepository).findById(testConversationId);
			verify(userEntityRepository).findById(testUserId);
			verify(messageMapper).toEntity(testMessage, testConversationEntity, testUserEntity);
			verify(messageEntityRepository).save(testMessageEntity);
			verify(messageMapper).toModel(testMessageEntity);
		}

		@Test
		@DisplayName("Should throw ConversationNotFoundException when conversation does not exist")
		void save_ShouldThrowConversationNotFoundException_WhenConversationDoesNotExist() {
			// Given
			when(conversationEntityRepository.findById(testConversationId))
					.thenReturn(Optional.empty());

			// When & Then
			ConversationNotFoundException exception =
					assertThrows(
							ConversationNotFoundException.class,
							() -> {
								messageRepository.save(testMessage);
							});

			assertNotNull(exception);
			verify(conversationEntityRepository).findById(testConversationId);
		}

		@Test
		@DisplayName("Should throw UserNotFoundException when sender does not exist")
		void save_ShouldThrowUserNotFoundException_WhenSenderDoesNotExist() {
			// Given
			when(conversationEntityRepository.findById(testConversationId))
					.thenReturn(Optional.of(testConversationEntity));
			when(userEntityRepository.findById(testUserId)).thenReturn(Optional.empty());

			// When & Then
			UserNotFoundException exception =
					assertThrows(
							UserNotFoundException.class,
							() -> {
								messageRepository.save(testMessage);
							});

			assertNotNull(exception);
			verify(conversationEntityRepository).findById(testConversationId);
			verify(userEntityRepository).findById(testUserId);
		}
	}

	@Nested
	@DisplayName("Find By ID Tests")
	class FindByIdTests {

		@Test
		@DisplayName("Should return message when found by ID")
		void findById_ShouldReturnMessage_WhenMessageExists() {
			// Given
			when(messageEntityRepository.findById(testMessageId))
					.thenReturn(Optional.of(testMessageEntity));
			when(messageMapper.toModel(testMessageEntity)).thenReturn(testMessage);

			// When
			Optional<Message> foundMessage = messageRepository.findById(testMessageId);

			// Then
			assertTrue(foundMessage.isPresent());
			assertEquals(testMessageId, foundMessage.get().getId());
			assertEquals(testMessage.getContent(), foundMessage.get().getContent());
			verify(messageEntityRepository).findById(testMessageId);
			verify(messageMapper).toModel(testMessageEntity);
		}

		@Test
		@DisplayName("Should return empty optional when no message found by ID")
		void findById_ShouldReturnEmpty_WhenMessageDoesNotExist() {
			// Given
			UUID nonExistentId = UUID.randomUUID();
			when(messageEntityRepository.findById(nonExistentId)).thenReturn(Optional.empty());

			// When
			Optional<Message> foundMessage = messageRepository.findById(nonExistentId);

			// Then
			assertFalse(foundMessage.isPresent());
			verify(messageEntityRepository).findById(nonExistentId);
		}
	}

	@Nested
	@DisplayName("Find By Conversation ID And Sent At After Tests")
	class FindByConversationIdAndSentAtAfterTests {

		@Test
		@DisplayName("Should return messages sent after specified time")
		void findByConversationIdAndSentAtAfter_ShouldReturnMessages_WhenMessagesExistAfterTime() {
			// Given
			LocalDateTime sentAtAfter = LocalDateTime.now().minusHours(1);
			List<MessageEntity> messageEntities = Arrays.asList(testMessageEntity);

			when(messageEntityRepository.findByConversationIdAndSendAtAfterOrderBySentAtAsc(
							testConversationId, sentAtAfter))
					.thenReturn(messageEntities);
			when(messageMapper.toModel(testMessageEntity)).thenReturn(testMessage);

			// When
			List<Message> messages =
					messageRepository.findByConversationIdAndSentAtAfter(
							testConversationId, sentAtAfter);

			// Then
			assertNotNull(messages);
			assertEquals(1, messages.size());
			assertEquals(testMessage.getId(), messages.get(0).getId());
			verify(messageEntityRepository)
					.findByConversationIdAndSendAtAfterOrderBySentAtAsc(
							testConversationId, sentAtAfter);
			verify(messageMapper).toModel(testMessageEntity);
		}

		@Test
		@DisplayName("Should return empty list when no messages exist after specified time")
		void
				findByConversationIdAndSentAtAfter_ShouldReturnEmptyList_WhenNoMessagesExistAfterTime() {
			// Given
			LocalDateTime sentAtAfter = LocalDateTime.now().minusHours(1);
			when(messageEntityRepository.findByConversationIdAndSendAtAfterOrderBySentAtAsc(
							testConversationId, sentAtAfter))
					.thenReturn(Collections.emptyList());

			// When
			List<Message> messages =
					messageRepository.findByConversationIdAndSentAtAfter(
							testConversationId, sentAtAfter);

			// Then
			assertNotNull(messages);
			assertTrue(messages.isEmpty());
			verify(messageEntityRepository)
					.findByConversationIdAndSendAtAfterOrderBySentAtAsc(
							testConversationId, sentAtAfter);
		}
	}

	@Nested
	@DisplayName("Delete Tests")
	class DeleteTests {

		@Test
		@DisplayName("Should delete message successfully")
		void delete_ShouldDeleteMessage_WhenValidIdProvided() {
			// Given
			doNothing().when(messageEntityRepository).deleteById(testMessageId);

			// When
			messageRepository.delete(testMessageId);

			// Then
			verify(messageEntityRepository).deleteById(testMessageId);
		}

		@Test
		@DisplayName("Should handle deletion of non-existent message")
		void delete_ShouldHandleNonExistentMessage_WhenIdDoesNotExist() {
			// Given
			UUID nonExistentId = UUID.randomUUID();
			doNothing().when(messageEntityRepository).deleteById(nonExistentId);

			// When
			messageRepository.delete(nonExistentId);

			// Then
			verify(messageEntityRepository).deleteById(nonExistentId);
		}
	}

	@Nested
	@DisplayName("Find Latest By Conversation ID Tests")
	class FindLatestByConversationIdTests {

		@Test
		@DisplayName("Should return latest message when conversation has messages")
		void findLatestByConversationId_ShouldReturnLatestMessage_WhenConversationHasMessages() {
			// Given
			when(messageEntityRepository.findByConversationIdOrderBySentAtDesc(testConversationId))
					.thenReturn(Optional.of(testMessageEntity));
			when(messageMapper.toModel(testMessageEntity)).thenReturn(testMessage);

			// When
			Optional<Message> latestMessage =
					messageRepository.findLatestByConversationId(testConversationId);

			// Then
			assertTrue(latestMessage.isPresent());
			assertEquals(testMessage.getId(), latestMessage.get().getId());
			verify(messageEntityRepository)
					.findByConversationIdOrderBySentAtDesc(testConversationId);
			verify(messageMapper).toModel(testMessageEntity);
		}

		@Test
		@DisplayName("Should return empty when conversation has no messages")
		void findLatestByConversationId_ShouldReturnEmpty_WhenConversationHasNoMessages() {
			// Given
			when(messageEntityRepository.findByConversationIdOrderBySentAtDesc(testConversationId))
					.thenReturn(Optional.empty());

			// When
			Optional<Message> latestMessage =
					messageRepository.findLatestByConversationId(testConversationId);

			// Then
			assertFalse(latestMessage.isPresent());
			verify(messageEntityRepository)
					.findByConversationIdOrderBySentAtDesc(testConversationId);
		}
	}

	@Nested
	@DisplayName("Find Latest Messages For Conversations Tests")
	class FindLatestMessagesForConversationsTests {

		@Test
		@DisplayName("Should return latest messages for multiple conversations")
		void
				findLatestMessagesForConversations_ShouldReturnLatestMessages_WhenConversationsHaveMessages() {
			// Given
			UUID conversationId2 = UUID.randomUUID();
			List<UUID> conversationIds = Arrays.asList(testConversationId, conversationId2);

			UUID messageId2 = UUID.randomUUID();
			List<UUID> latestMessageIds = Arrays.asList(testMessageId, messageId2);

			Message message2 =
					messageTestDataBuilder
							.withId(messageId2)
							.withConversationId(conversationId2)
							.withContent("Second message")
							.build();

			MessageEntity messageEntity2 = new MessageEntity();
			messageEntity2.setId(messageId2);
			messageEntity2.setContent("Second message");

			List<MessageEntity> latestEntities = Arrays.asList(testMessageEntity, messageEntity2);

			when(messageEntityRepository.findLatestMessageIdsForConversationIds(conversationIds))
					.thenReturn(latestMessageIds);
			when(messageEntityRepository.findMessagesByIdsWithSender(latestMessageIds))
					.thenReturn(latestEntities);
			when(messageMapper.toModel(testMessageEntity)).thenReturn(testMessage);
			when(messageMapper.toModel(messageEntity2)).thenReturn(message2);

			// When
			Map<UUID, Message> latestMessages =
					messageRepository.findLatestMessagesForConversations(conversationIds);

			// Then
			assertNotNull(latestMessages);
			assertEquals(2, latestMessages.size());
			assertTrue(latestMessages.containsKey(testConversationId));
			assertTrue(latestMessages.containsKey(conversationId2));
			assertEquals(testMessage.getId(), latestMessages.get(testConversationId).getId());
			assertEquals(message2.getId(), latestMessages.get(conversationId2).getId());
			verify(messageEntityRepository).findLatestMessageIdsForConversationIds(conversationIds);
			verify(messageEntityRepository).findMessagesByIdsWithSender(latestMessageIds);
		}

		@Test
		@DisplayName("Should return empty map when conversations have no messages")
		void
				findLatestMessagesForConversations_ShouldReturnEmptyMap_WhenConversationsHaveNoMessages() {
			// Given
			List<UUID> conversationIds = Arrays.asList(testConversationId);
			when(messageEntityRepository.findLatestMessageIdsForConversationIds(conversationIds))
					.thenReturn(Collections.emptyList());

			// When
			Map<UUID, Message> latestMessages =
					messageRepository.findLatestMessagesForConversations(conversationIds);

			// Then
			assertNotNull(latestMessages);
			assertTrue(latestMessages.isEmpty());
			verify(messageEntityRepository).findLatestMessageIdsForConversationIds(conversationIds);
		}

		@Test
		@DisplayName("Should handle empty conversation IDs list")
		void
				findLatestMessagesForConversations_ShouldReturnEmptyMap_WhenConversationIdsListIsEmpty() {
			// Given
			List<UUID> emptyConversationIds = Collections.emptyList();

			// When
			Map<UUID, Message> latestMessages =
					messageRepository.findLatestMessagesForConversations(emptyConversationIds);

			// Then
			assertNotNull(latestMessages);
			assertTrue(latestMessages.isEmpty());
			// Should not call repository methods when list is empty
			verify(messageEntityRepository, never()).findLatestMessageIdsForConversationIds(any());
			verify(messageEntityRepository, never()).findMessagesByIdsWithSender(any());
		}

		@Test
		@DisplayName("Should handle null conversation IDs list")
		void
				findLatestMessagesForConversations_ShouldReturnEmptyMap_WhenConversationIdsListIsNull() {
			// When
			Map<UUID, Message> latestMessages =
					messageRepository.findLatestMessagesForConversations(null);

			// Then
			assertNotNull(latestMessages);
			assertTrue(latestMessages.isEmpty());
			// Should not call repository methods when list is null
			verify(messageEntityRepository, never()).findLatestMessageIdsForConversationIds(any());
			verify(messageEntityRepository, never()).findMessagesByIdsWithSender(any());
		}

		@Test
		@DisplayName("Should handle empty message IDs from first query")
		void findLatestMessagesForConversations_ShouldReturnEmptyMap_WhenNoLatestMessageIdsFound() {
			// Given
			List<UUID> conversationIds = Arrays.asList(testConversationId);
			when(messageEntityRepository.findLatestMessageIdsForConversationIds(conversationIds))
					.thenReturn(Collections.emptyList());

			// When
			Map<UUID, Message> latestMessages =
					messageRepository.findLatestMessagesForConversations(conversationIds);

			// Then
			assertNotNull(latestMessages);
			assertTrue(latestMessages.isEmpty());
			verify(messageEntityRepository).findLatestMessageIdsForConversationIds(conversationIds);
			verify(messageEntityRepository, never()).findMessagesByIdsWithSender(any());
		}
	}
}
