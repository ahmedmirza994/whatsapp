/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.repository;

import static com.ah.whatsapp.mapper.ConversationParticipantTestDataBuilder.aConversationParticipant;
import static com.ah.whatsapp.mapper.ConversationTestDataBuilder.aConversation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import com.ah.whatsapp.mapper.ConversationMapper;
import com.ah.whatsapp.model.Conversation;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.model.Message;
import com.ah.whatsapp.repository.entity.ConversationEntityRepository;
import com.ah.whatsapp.repository.impl.ConversationRepositoryImpl;

/**
 * Comprehensive Unit Tests for ConversationRepository
 * Following industry best practices for repository layer testing
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationRepository Tests")
class ConversationRepositoryTest {

	@Mock private ConversationEntityRepository conversationEntityRepository;

	@Mock private ConversationParticipantRepository conversationParticipantRepository;

	@Mock private MessageRepository messageRepository;

	@Mock private ConversationMapper conversationMapper;

	@InjectMocks private ConversationRepositoryImpl conversationRepository;

	private Conversation testConversation;
	private ConversationEntity testConversationEntity;
	private UUID testConversationId;
	private UUID testUserId1;
	private UUID testUserId2;

	@BeforeEach
	void setUp() {
		testConversationId = UUID.randomUUID();
		testUserId1 = UUID.randomUUID();
		testUserId2 = UUID.randomUUID();

		// Using ConversationTestDataBuilder for consistent test data
		testConversation = aConversation().withId(testConversationId).build();

		testConversationEntity = aConversation().withId(testConversationId).buildEntity();
	}

	@Nested
	@DisplayName("Save Conversation Tests")
	class SaveConversationTests {

		@Test
		@DisplayName("Should save conversation successfully and return mapped model")
		void save_ShouldReturnSavedConversation_WhenValidConversationProvided() {
			// Given
			when(conversationMapper.toEntity(testConversation)).thenReturn(testConversationEntity);
			when(conversationEntityRepository.save(testConversationEntity))
					.thenReturn(testConversationEntity);
			when(conversationMapper.toModel(testConversationEntity)).thenReturn(testConversation);

			// When
			Conversation savedConversation = conversationRepository.save(testConversation);

			// Then
			assertNotNull(savedConversation);
			assertEquals(testConversation.getId(), savedConversation.getId());

			// Verify interactions
			verify(conversationMapper).toEntity(testConversation);
			verify(conversationEntityRepository).save(testConversationEntity);
			verify(conversationMapper).toModel(testConversationEntity);
		}

		@Test
		@DisplayName("Should handle conversation with minimal required fields")
		void save_ShouldHandleMinimalConversation_WhenOnlyRequiredFieldsProvided() {
			// Given
			Conversation minimalConversation =
					aConversation()
							.withParticipants(Collections.emptyList())
							.withMessages(Collections.emptyList())
							.build();

			ConversationEntity minimalEntity = aConversation().buildEntity();

			when(conversationMapper.toEntity(minimalConversation)).thenReturn(minimalEntity);
			when(conversationEntityRepository.save(minimalEntity)).thenReturn(minimalEntity);
			when(conversationMapper.toModel(minimalEntity)).thenReturn(minimalConversation);

			// When
			Conversation savedConversation = conversationRepository.save(minimalConversation);

			// Then
			assertNotNull(savedConversation);
			verify(conversationEntityRepository).save(minimalEntity);
		}
	}

	@Nested
	@DisplayName("Find By ID Tests")
	class FindByIdTests {

		@Test
		@DisplayName(
				"Should return conversation with participants and last message when found by ID")
		void findById_ShouldReturnConversationWithDetails_WhenConversationExists() {
			// Given
			when(conversationEntityRepository.findById(testConversationId))
					.thenReturn(Optional.of(testConversationEntity));
			when(conversationMapper.toModel(testConversationEntity)).thenReturn(testConversation);
			when(messageRepository.findLatestByConversationId(testConversationId))
					.thenReturn(Optional.empty());
			when(conversationParticipantRepository.findByConversationIdAndIsActiveTrue(
							testConversationId))
					.thenReturn(Collections.emptyList());

			// When
			Optional<Conversation> foundConversation =
					conversationRepository.findById(testConversationId);

			// Then
			assertTrue(foundConversation.isPresent());
			assertEquals(testConversationId, foundConversation.get().getId());
			verify(conversationEntityRepository).findById(testConversationId);
			verify(conversationMapper).toModel(testConversationEntity);
			verify(messageRepository).findLatestByConversationId(testConversationId);
			verify(conversationParticipantRepository)
					.findByConversationIdAndIsActiveTrue(testConversationId);
		}

		@Test
		@DisplayName("Should return empty optional when no conversation found by ID")
		void findById_ShouldReturnEmpty_WhenNoConversationExists() {
			// Given
			UUID nonExistentId = UUID.randomUUID();
			when(conversationEntityRepository.findById(nonExistentId)).thenReturn(Optional.empty());

			// When
			Optional<Conversation> foundConversation =
					conversationRepository.findById(nonExistentId);

			// Then
			assertFalse(foundConversation.isPresent());
			verify(conversationEntityRepository).findById(nonExistentId);
		}
	}

	@Nested
	@DisplayName("Find By User ID Tests")
	class FindByUserIdTests {

		@Test
		@DisplayName("Should return conversations for user with loaded details")
		void findByUserId_ShouldReturnConversationsWithDetails_WhenUserHasConversations() {
			// Given
			List<ConversationEntity> entities = Arrays.asList(testConversationEntity);
			List<UUID> conversationIds = Arrays.asList(testConversationId);
			Map<UUID, Message> lastMessagesMap = new HashMap<>();
			Map<UUID, List<ConversationParticipant>> participantsMap = new HashMap<>();

			// Create a participant that matches testUserId1 to avoid NullPointerException
			ConversationParticipant participant1 =
					aConversationParticipant()
							.withConversationId(testConversationId)
							.withParticipantId(testUserId1)
							.withParticipantName("Test User 1")
							.withParticipantEmail("user1@example.com")
							.build();

			participantsMap.put(testConversationId, Arrays.asList(participant1));

			when(conversationEntityRepository.findConversationsByUserId(testUserId1))
					.thenReturn(entities);
			when(messageRepository.findLatestMessagesForConversations(conversationIds))
					.thenReturn(lastMessagesMap);
			when(conversationParticipantRepository.findParticipantsForConversationsAndIsActiveTrue(
							conversationIds))
					.thenReturn(participantsMap);
			when(conversationMapper.toModel(testConversationEntity)).thenReturn(testConversation);

			// When
			List<Conversation> conversations = conversationRepository.findByUserId(testUserId1);

			// Then
			assertNotNull(conversations);
			assertEquals(1, conversations.size());
			verify(conversationEntityRepository).findConversationsByUserId(testUserId1);
			verify(messageRepository).findLatestMessagesForConversations(conversationIds);
			verify(conversationParticipantRepository)
					.findParticipantsForConversationsAndIsActiveTrue(conversationIds);
		}

		@Test
		@DisplayName("Should return empty list when user has no conversations")
		void findByUserId_ShouldReturnEmptyList_WhenUserHasNoConversations() {
			// Given
			when(conversationEntityRepository.findConversationsByUserId(testUserId1))
					.thenReturn(Collections.emptyList());
			when(messageRepository.findLatestMessagesForConversations(Collections.emptyList()))
					.thenReturn(Collections.emptyMap());
			when(conversationParticipantRepository.findParticipantsForConversationsAndIsActiveTrue(
							Collections.emptyList()))
					.thenReturn(Collections.emptyMap());

			// When
			List<Conversation> conversations = conversationRepository.findByUserId(testUserId1);

			// Then
			assertNotNull(conversations);
			assertTrue(conversations.isEmpty());
			verify(conversationEntityRepository).findConversationsByUserId(testUserId1);
		}
	}

	@Nested
	@DisplayName("Delete Tests")
	class DeleteTests {

		@Test
		@DisplayName("Should delete conversation successfully")
		void delete_ShouldDeleteConversation_WhenValidIdProvided() {
			// Given
			doNothing().when(conversationEntityRepository).deleteById(testConversationId);

			// When
			conversationRepository.delete(testConversationId);

			// Then
			verify(conversationEntityRepository).deleteById(testConversationId);
		}

		@Test
		@DisplayName("Should handle deletion of non-existent conversation")
		void delete_ShouldHandleNonExistentConversation_WhenIdDoesNotExist() {
			// Given
			UUID nonExistentId = UUID.randomUUID();
			doNothing().when(conversationEntityRepository).deleteById(nonExistentId);

			// When
			conversationRepository.delete(nonExistentId);

			// Then
			verify(conversationEntityRepository).deleteById(nonExistentId);
		}
	}

	@Nested
	@DisplayName("Exists By ID Tests")
	class ExistsByIdTests {

		@Test
		@DisplayName("Should return true when conversation exists with given ID")
		void existsById_ShouldReturnTrue_WhenConversationExists() {
			// Given
			when(conversationEntityRepository.existsById(testConversationId)).thenReturn(true);

			// When
			boolean exists = conversationRepository.existsById(testConversationId);

			// Then
			assertTrue(exists);
			verify(conversationEntityRepository).existsById(testConversationId);
		}

		@Test
		@DisplayName("Should return false when no conversation exists with given ID")
		void existsById_ShouldReturnFalse_WhenConversationDoesNotExist() {
			// Given
			UUID nonExistentId = UUID.randomUUID();
			when(conversationEntityRepository.existsById(nonExistentId)).thenReturn(false);

			// When
			boolean exists = conversationRepository.existsById(nonExistentId);

			// Then
			assertFalse(exists);
			verify(conversationEntityRepository).existsById(nonExistentId);
		}
	}

	@Nested
	@DisplayName("Find Direct Conversation Tests")
	class FindDirectConversationTests {

		@Test
		@DisplayName("Should return direct conversation when it exists between two users")
		void
				findDirectConversationBetweenUsers_ShouldReturnConversation_WhenDirectConversationExists() {
			// Given
			List<UUID> conversationIds = Arrays.asList(testConversationId);
			when(conversationEntityRepository.findConversationsWithParticipants(
							Arrays.asList(testUserId1, testUserId2), 2L))
					.thenReturn(conversationIds);
			when(conversationEntityRepository.findById(testConversationId))
					.thenReturn(Optional.of(testConversationEntity));
			when(conversationMapper.toModel(testConversationEntity)).thenReturn(testConversation);
			when(messageRepository.findLatestByConversationId(testConversationId))
					.thenReturn(Optional.empty());
			when(conversationParticipantRepository.findByConversationIdAndIsActiveTrue(
							testConversationId))
					.thenReturn(Collections.emptyList());

			// When
			Optional<Conversation> foundConversation =
					conversationRepository.findDirectConversationBetweenUsers(
							testUserId1, testUserId2);

			// Then
			assertTrue(foundConversation.isPresent());
			assertEquals(testConversationId, foundConversation.get().getId());
			verify(conversationEntityRepository)
					.findConversationsWithParticipants(Arrays.asList(testUserId1, testUserId2), 2L);
		}

		@Test
		@DisplayName("Should return empty when no direct conversation exists between two users")
		void findDirectConversationBetweenUsers_ShouldReturnEmpty_WhenNoDirectConversationExists() {
			// Given
			when(conversationEntityRepository.findConversationsWithParticipants(
							Arrays.asList(testUserId1, testUserId2), 2L))
					.thenReturn(Collections.emptyList());

			// When
			Optional<Conversation> foundConversation =
					conversationRepository.findDirectConversationBetweenUsers(
							testUserId1, testUserId2);

			// Then
			assertFalse(foundConversation.isPresent());
			verify(conversationEntityRepository)
					.findConversationsWithParticipants(Arrays.asList(testUserId1, testUserId2), 2L);
		}

		@Test
		@DisplayName("Should handle multiple potential conversations by returning the first one")
		void
				findDirectConversationBetweenUsers_ShouldReturnFirstConversation_WhenMultipleConversationsExist() {
			// Given
			UUID secondConversationId = UUID.randomUUID();
			List<UUID> conversationIds = Arrays.asList(testConversationId, secondConversationId);
			when(conversationEntityRepository.findConversationsWithParticipants(
							Arrays.asList(testUserId1, testUserId2), 2L))
					.thenReturn(conversationIds);
			when(conversationEntityRepository.findById(testConversationId))
					.thenReturn(Optional.of(testConversationEntity));
			when(conversationMapper.toModel(testConversationEntity)).thenReturn(testConversation);
			when(messageRepository.findLatestByConversationId(testConversationId))
					.thenReturn(Optional.empty());
			when(conversationParticipantRepository.findByConversationIdAndIsActiveTrue(
							testConversationId))
					.thenReturn(Collections.emptyList());

			// When
			Optional<Conversation> foundConversation =
					conversationRepository.findDirectConversationBetweenUsers(
							testUserId1, testUserId2);

			// Then
			assertTrue(foundConversation.isPresent());
			assertEquals(testConversationId, foundConversation.get().getId());
			verify(conversationEntityRepository)
					.findConversationsWithParticipants(Arrays.asList(testUserId1, testUserId2), 2L);
		}
	}
}
