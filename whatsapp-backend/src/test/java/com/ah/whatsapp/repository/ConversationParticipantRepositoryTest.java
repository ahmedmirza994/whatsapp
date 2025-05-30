/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.repository;

import static com.ah.whatsapp.mapper.ConversationParticipantTestDataBuilder.aConversationParticipant;
import static com.ah.whatsapp.mapper.ConversationTestDataBuilder.aConversation;
import static com.ah.whatsapp.mapper.UserTestDataBuilder.aUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.ah.whatsapp.entity.ConversationParticipantEntity;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.exception.ConversationNotFoundException;
import com.ah.whatsapp.exception.UserNotFoundException;
import com.ah.whatsapp.mapper.ConversationParticipantMapper;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.repository.entity.ConversationEntityRepository;
import com.ah.whatsapp.repository.entity.ConversationParticipantEntityRepository;
import com.ah.whatsapp.repository.entity.UserEntityRepository;
import com.ah.whatsapp.repository.impl.ConversationParticipantRepositoryImpl;

/**
 * Comprehensive test suite for ConversationParticipantRepository
 * Tests all repository methods following industry best practices
 *
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationParticipantRepository Tests")
class ConversationParticipantRepositoryTest {

	@Mock private ConversationParticipantEntityRepository participantEntityRepository;

	@Mock private ConversationEntityRepository conversationEntityRepository;

	@Mock private UserEntityRepository userEntityRepository;

	@Mock private ConversationParticipantMapper participantMapper;

	@InjectMocks private ConversationParticipantRepositoryImpl conversationParticipantRepository;

	private UUID conversationId;
	private UUID userId;
	private UUID participantId;
	private ConversationParticipant testParticipant;
	private ConversationParticipantEntity testParticipantEntity;
	private ConversationEntity testConversationEntity;
	private UserEntity testUserEntity;

	@BeforeEach
	void setUp() {
		conversationId = UUID.randomUUID();
		userId = UUID.randomUUID();
		participantId = UUID.randomUUID();

		testParticipant =
				aConversationParticipant()
						.withId(participantId)
						.withConversationId(conversationId)
						.withParticipantId(userId)
						.withParticipantName("John Doe")
						.withParticipantEmail("john.doe@example.com")
						.build();

		testConversationEntity = aConversation().withId(conversationId).buildEntity();

		testUserEntity =
				aUser().withId(userId)
						.withName("John Doe")
						.withEmail("john.doe@example.com")
						.buildEntity();

		testParticipantEntity =
				aConversationParticipant()
						.withId(participantId)
						.withConversationId(conversationId)
						.withParticipantId(userId)
						.buildEntity(testConversationEntity, testUserEntity);
	}

	@Nested
	@DisplayName("Save Operation Tests")
	class SaveOperationTests {

		@Test
		@DisplayName("Should save conversation participant successfully")
		void save_WithValidParticipant_ShouldReturnSavedParticipant() {
			// Given
			when(conversationEntityRepository.findById(conversationId))
					.thenReturn(Optional.of(testConversationEntity));
			when(userEntityRepository.findById(userId)).thenReturn(Optional.of(testUserEntity));
			when(participantMapper.toEntity(
							testParticipant, testConversationEntity, testUserEntity))
					.thenReturn(testParticipantEntity);
			when(participantEntityRepository.save(testParticipantEntity))
					.thenReturn(testParticipantEntity);
			when(participantMapper.toModel(testParticipantEntity)).thenReturn(testParticipant);

			// When
			ConversationParticipant result =
					conversationParticipantRepository.save(testParticipant);

			// Then
			assertNotNull(result);
			assertEquals(testParticipant, result);
			verify(conversationEntityRepository).findById(conversationId);
			verify(userEntityRepository).findById(userId);
			verify(participantMapper)
					.toEntity(testParticipant, testConversationEntity, testUserEntity);
			verify(participantEntityRepository).save(testParticipantEntity);
			verify(participantMapper).toModel(testParticipantEntity);
		}

		@Test
		@DisplayName("Should throw ConversationNotFoundException when conversation not found")
		void save_WithNonExistentConversation_ShouldThrowConversationNotFoundException() {
			// Given
			when(conversationEntityRepository.findById(conversationId))
					.thenReturn(Optional.empty());

			// When & Then
			ConversationNotFoundException exception =
					assertThrows(
							ConversationNotFoundException.class,
							() -> conversationParticipantRepository.save(testParticipant));

			assertEquals("Conversation not found: " + conversationId, exception.getMessage());
			verify(conversationEntityRepository).findById(conversationId);
			verify(userEntityRepository, never()).findById(any());
			verify(participantEntityRepository, never()).save(any());
		}

		@Test
		@DisplayName("Should throw UserNotFoundException when user not found")
		void save_WithNonExistentUser_ShouldThrowUserNotFoundException() {
			// Given
			when(conversationEntityRepository.findById(conversationId))
					.thenReturn(Optional.of(testConversationEntity));
			when(userEntityRepository.findById(userId)).thenReturn(Optional.empty());

			// When & Then
			UserNotFoundException exception =
					assertThrows(
							UserNotFoundException.class,
							() -> conversationParticipantRepository.save(testParticipant));

			assertEquals("User not found: " + userId, exception.getMessage());
			verify(conversationEntityRepository).findById(conversationId);
			verify(userEntityRepository).findById(userId);
			verify(participantEntityRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("Find By ID Tests")
	class FindByIdTests {

		@Test
		@DisplayName("Should return participant when found by ID")
		void findById_WithExistingId_ShouldReturnParticipant() {
			// Given
			when(participantEntityRepository.findById(participantId))
					.thenReturn(Optional.of(testParticipantEntity));
			when(participantMapper.toModel(testParticipantEntity)).thenReturn(testParticipant);

			// When
			Optional<ConversationParticipant> result =
					conversationParticipantRepository.findById(participantId);

			// Then
			assertTrue(result.isPresent());
			assertEquals(testParticipant, result.get());
			verify(participantEntityRepository).findById(participantId);
			verify(participantMapper).toModel(testParticipantEntity);
		}

		@Test
		@DisplayName("Should return empty optional when participant not found")
		void findById_WithNonExistentId_ShouldReturnEmptyOptional() {
			// Given
			when(participantEntityRepository.findById(participantId)).thenReturn(Optional.empty());

			// When
			Optional<ConversationParticipant> result =
					conversationParticipantRepository.findById(participantId);

			// Then
			assertFalse(result.isPresent());
			verify(participantEntityRepository).findById(participantId);
			verify(participantMapper, never()).toModel(any());
		}
	}

	@Nested
	@DisplayName("Find By Conversation ID and Active Tests")
	class FindByConversationIdAndActiveTests {

		@Test
		@DisplayName("Should return list of active participants for conversation")
		void findByConversationIdAndIsActiveTrue_WithActiveParticipants_ShouldReturnList() {
			// Given
			List<ConversationParticipantEntity> entities =
					Arrays.asList(
							testParticipantEntity,
							aConversationParticipant()
									.withConversationId(conversationId)
									.withParticipantId(UUID.randomUUID())
									.buildEntity(testConversationEntity, testUserEntity));
			List<ConversationParticipant> expectedParticipants =
					Arrays.asList(
							testParticipant,
							aConversationParticipant()
									.withConversationId(conversationId)
									.withParticipantId(UUID.randomUUID())
									.build());

			when(participantEntityRepository.findByConversationIdAndIsActiveTrueWithUser(
							conversationId))
					.thenReturn(entities);
			when(participantMapper.toModel(entities.get(0)))
					.thenReturn(expectedParticipants.get(0));
			when(participantMapper.toModel(entities.get(1)))
					.thenReturn(expectedParticipants.get(1));

			// When
			List<ConversationParticipant> result =
					conversationParticipantRepository.findByConversationIdAndIsActiveTrue(
							conversationId);

			// Then
			assertNotNull(result);
			assertEquals(2, result.size());
			assertEquals(expectedParticipants, result);
			verify(participantEntityRepository)
					.findByConversationIdAndIsActiveTrueWithUser(conversationId);
		}

		@Test
		@DisplayName("Should return empty list when no active participants found")
		void findByConversationIdAndIsActiveTrue_WithNoActiveParticipants_ShouldReturnEmptyList() {
			// Given
			when(participantEntityRepository.findByConversationIdAndIsActiveTrueWithUser(
							conversationId))
					.thenReturn(Collections.emptyList());

			// When
			List<ConversationParticipant> result =
					conversationParticipantRepository.findByConversationIdAndIsActiveTrue(
							conversationId);

			// Then
			assertNotNull(result);
			assertTrue(result.isEmpty());
			verify(participantEntityRepository)
					.findByConversationIdAndIsActiveTrueWithUser(conversationId);
		}
	}

	@Nested
	@DisplayName("Exists By Conversation ID and User ID Tests")
	class ExistsByConversationIdAndUserIdTests {

		@Test
		@DisplayName("Should return true when active participant exists")
		void
				existsByConversationIdAndUserIdAndIsActiveTrue_WithExistingActiveParticipant_ShouldReturnTrue() {
			// Given
			when(participantEntityRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, userId))
					.thenReturn(true);

			// When
			boolean result =
					conversationParticipantRepository
							.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId);

			// Then
			assertTrue(result);
			verify(participantEntityRepository)
					.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId);
		}

		@Test
		@DisplayName("Should return false when no active participant exists")
		void
				existsByConversationIdAndUserIdAndIsActiveTrue_WithNoActiveParticipant_ShouldReturnFalse() {
			// Given
			when(participantEntityRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, userId))
					.thenReturn(false);

			// When
			boolean result =
					conversationParticipantRepository
							.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId);

			// Then
			assertFalse(result);
			verify(participantEntityRepository)
					.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId);
		}
	}

	@Nested
	@DisplayName("Find Participants For Conversations Tests")
	class FindParticipantsForConversationsTests {

		@Test
		@DisplayName("Should return grouped participants for multiple conversations")
		void
				findParticipantsForConversationsAndIsActiveTrue_WithMultipleConversations_ShouldReturnGroupedMap() {
			// Given
			UUID conversation1Id = UUID.randomUUID();
			UUID conversation2Id = UUID.randomUUID();
			List<UUID> conversationIds = Arrays.asList(conversation1Id, conversation2Id);

			ConversationParticipant participant1 =
					aConversationParticipant()
							.withConversationId(conversation1Id)
							.withParticipantId(UUID.randomUUID())
							.build();
			ConversationParticipant participant2 =
					aConversationParticipant()
							.withConversationId(conversation1Id)
							.withParticipantId(UUID.randomUUID())
							.build();
			ConversationParticipant participant3 =
					aConversationParticipant()
							.withConversationId(conversation2Id)
							.withParticipantId(UUID.randomUUID())
							.build();

			List<ConversationParticipantEntity> entities =
					Arrays.asList(
							mock(ConversationParticipantEntity.class),
							mock(ConversationParticipantEntity.class),
							mock(ConversationParticipantEntity.class));

			when(participantEntityRepository.findByConversationIdInAndIsActiveTrueWithUser(
							conversationIds))
					.thenReturn(entities);
			when(participantMapper.toModel(entities.get(0))).thenReturn(participant1);
			when(participantMapper.toModel(entities.get(1))).thenReturn(participant2);
			when(participantMapper.toModel(entities.get(2))).thenReturn(participant3);

			// When
			Map<UUID, List<ConversationParticipant>> result =
					conversationParticipantRepository
							.findParticipantsForConversationsAndIsActiveTrue(conversationIds);

			// Then
			assertNotNull(result);
			assertEquals(2, result.size());
			assertTrue(result.containsKey(conversation1Id));
			assertTrue(result.containsKey(conversation2Id));
			assertEquals(2, result.get(conversation1Id).size());
			assertEquals(1, result.get(conversation2Id).size());
			verify(participantEntityRepository)
					.findByConversationIdInAndIsActiveTrueWithUser(conversationIds);
		}

		@Test
		@DisplayName("Should return empty map when conversation IDs list is null")
		void
				findParticipantsForConversationsAndIsActiveTrue_WithNullConversationIds_ShouldReturnEmptyMap() {
			// When
			Map<UUID, List<ConversationParticipant>> result =
					conversationParticipantRepository
							.findParticipantsForConversationsAndIsActiveTrue(null);

			// Then
			assertNotNull(result);
			assertTrue(result.isEmpty());
			verify(participantEntityRepository, never())
					.findByConversationIdInAndIsActiveTrueWithUser(anyList());
		}

		@Test
		@DisplayName("Should return empty map when conversation IDs list is empty")
		void
				findParticipantsForConversationsAndIsActiveTrue_WithEmptyConversationIds_ShouldReturnEmptyMap() {
			// When
			Map<UUID, List<ConversationParticipant>> result =
					conversationParticipantRepository
							.findParticipantsForConversationsAndIsActiveTrue(
									Collections.emptyList());

			// Then
			assertNotNull(result);
			assertTrue(result.isEmpty());
			verify(participantEntityRepository, never())
					.findByConversationIdInAndIsActiveTrueWithUser(anyList());
		}
	}

	@Nested
	@DisplayName("Find By Conversation ID and User ID and Active Tests")
	class FindByConversationIdAndUserIdAndActiveTests {

		@Test
		@DisplayName("Should return active participant when found")
		void
				findByConversationIdAndUserIdAndIsActiveTrue_WithActiveParticipant_ShouldReturnParticipant() {
			// Given
			when(participantEntityRepository.findByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, userId))
					.thenReturn(Optional.of(testParticipantEntity));
			when(participantMapper.toModel(testParticipantEntity)).thenReturn(testParticipant);

			// When
			Optional<ConversationParticipant> result =
					conversationParticipantRepository.findByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, userId);

			// Then
			assertTrue(result.isPresent());
			assertEquals(testParticipant, result.get());
			verify(participantEntityRepository)
					.findByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId);
			verify(participantMapper).toModel(testParticipantEntity);
		}

		@Test
		@DisplayName("Should return empty optional when no active participant found")
		void
				findByConversationIdAndUserIdAndIsActiveTrue_WithNoActiveParticipant_ShouldReturnEmptyOptional() {
			// Given
			when(participantEntityRepository.findByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, userId))
					.thenReturn(Optional.empty());

			// When
			Optional<ConversationParticipant> result =
					conversationParticipantRepository.findByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, userId);

			// Then
			assertFalse(result.isPresent());
			verify(participantEntityRepository)
					.findByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId);
			verify(participantMapper, never()).toModel(any());
		}
	}

	@Nested
	@DisplayName("Find By Conversation ID Tests")
	class FindByConversationIdTests {

		@Test
		@DisplayName("Should return all participants including inactive ones")
		void findByConversationId_WithAllParticipants_ShouldReturnCompleteList() {
			// Given
			ConversationParticipant inactiveParticipant =
					aConversationParticipant()
							.withConversationId(conversationId)
							.withParticipantId(UUID.randomUUID())
							.withInactiveState()
							.build();

			List<ConversationParticipantEntity> entities =
					Arrays.asList(
							testParticipantEntity,
							aConversationParticipant()
									.withInactiveState()
									.buildEntity(testConversationEntity, testUserEntity));

			when(participantEntityRepository.findByConversationIdWithUser(conversationId))
					.thenReturn(entities);
			when(participantMapper.toModel(entities.get(0))).thenReturn(testParticipant);
			when(participantMapper.toModel(entities.get(1))).thenReturn(inactiveParticipant);

			// When
			List<ConversationParticipant> result =
					conversationParticipantRepository.findByConversationId(conversationId);

			// Then
			assertNotNull(result);
			assertEquals(2, result.size());
			assertTrue(result.contains(testParticipant));
			assertTrue(result.contains(inactiveParticipant));
			verify(participantEntityRepository).findByConversationIdWithUser(conversationId);
		}

		@Test
		@DisplayName("Should return empty list when no participants found")
		void findByConversationId_WithNoParticipants_ShouldReturnEmptyList() {
			// Given
			when(participantEntityRepository.findByConversationIdWithUser(conversationId))
					.thenReturn(Collections.emptyList());

			// When
			List<ConversationParticipant> result =
					conversationParticipantRepository.findByConversationId(conversationId);

			// Then
			assertNotNull(result);
			assertTrue(result.isEmpty());
			verify(participantEntityRepository).findByConversationIdWithUser(conversationId);
		}
	}

	@Nested
	@DisplayName("Find By Conversation ID and User ID Tests")
	class FindByConversationIdAndUserIdTests {

		@Test
		@DisplayName("Should return participant when found by conversation and user ID")
		void findByConversationIdAndUserId_WithExistingParticipant_ShouldReturnParticipant() {
			// Given
			when(participantEntityRepository.findByConversationIdAndUserId(conversationId, userId))
					.thenReturn(Optional.of(testParticipantEntity));
			when(participantMapper.toModel(testParticipantEntity)).thenReturn(testParticipant);

			// When
			Optional<ConversationParticipant> result =
					conversationParticipantRepository.findByConversationIdAndUserId(
							conversationId, userId);

			// Then
			assertTrue(result.isPresent());
			assertEquals(testParticipant, result.get());
			verify(participantEntityRepository)
					.findByConversationIdAndUserId(conversationId, userId);
			verify(participantMapper).toModel(testParticipantEntity);
		}

		@Test
		@DisplayName("Should return empty optional when participant not found")
		void findByConversationIdAndUserId_WithNonExistentParticipant_ShouldReturnEmptyOptional() {
			// Given
			when(participantEntityRepository.findByConversationIdAndUserId(conversationId, userId))
					.thenReturn(Optional.empty());

			// When
			Optional<ConversationParticipant> result =
					conversationParticipantRepository.findByConversationIdAndUserId(
							conversationId, userId);

			// Then
			assertFalse(result.isPresent());
			verify(participantEntityRepository)
					.findByConversationIdAndUserId(conversationId, userId);
			verify(participantMapper, never()).toModel(any());
		}

		@Test
		@DisplayName("Should return inactive participant when found")
		void
				findByConversationIdAndUserId_WithInactiveParticipant_ShouldReturnInactiveParticipant() {
			// Given
			ConversationParticipant inactiveParticipant =
					aConversationParticipant()
							.withConversationId(conversationId)
							.withParticipantId(userId)
							.withInactiveState()
							.build();

			ConversationParticipantEntity inactiveEntity =
					aConversationParticipant()
							.withConversationId(conversationId)
							.withParticipantId(userId)
							.withInactiveState()
							.buildEntity(testConversationEntity, testUserEntity);

			when(participantEntityRepository.findByConversationIdAndUserId(conversationId, userId))
					.thenReturn(Optional.of(inactiveEntity));
			when(participantMapper.toModel(inactiveEntity)).thenReturn(inactiveParticipant);

			// When
			Optional<ConversationParticipant> result =
					conversationParticipantRepository.findByConversationIdAndUserId(
							conversationId, userId);

			// Then
			assertTrue(result.isPresent());
			assertEquals(inactiveParticipant, result.get());
			assertFalse(result.get().isActive());
			verify(participantEntityRepository)
					.findByConversationIdAndUserId(conversationId, userId);
			verify(participantMapper).toModel(inactiveEntity);
		}
	}
}
