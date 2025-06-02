/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.service.impl;

import static com.ah.whatsapp.dto.ConversationDtoTestDataBuilder.aConversationDto;
import static com.ah.whatsapp.mapper.ConversationParticipantTestDataBuilder.aConversationParticipant;
import static com.ah.whatsapp.mapper.ConversationTestDataBuilder.aConversation;
import static com.ah.whatsapp.mapper.UserTestDataBuilder.aUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import com.ah.whatsapp.dto.ConversationDto;
import com.ah.whatsapp.dto.CreateConversationRequest;
import com.ah.whatsapp.event.ConversationUpdateEvent;
import com.ah.whatsapp.exception.ConversationNotFoundException;
import com.ah.whatsapp.exception.UserNotFoundException;
import com.ah.whatsapp.mapper.ConversationMapper;
import com.ah.whatsapp.model.Conversation;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.ConversationParticipantRepository;
import com.ah.whatsapp.repository.ConversationRepository;
import com.ah.whatsapp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationServiceImpl Tests")
class ConversationServiceImplTest {

	@Mock private ConversationRepository conversationRepository;
	@Mock private UserRepository userRepository;
	@Mock private ConversationMapper conversationMapper;
	@Mock private ConversationParticipantRepository conversationParticipantRepository;
	@Mock private ApplicationEventPublisher applicationEventPublisher;

	@InjectMocks private ConversationServiceImpl conversationService;

	private UUID testUserId1;
	private UUID testUserId2;
	private UUID testConversationId;
	private User testUser1;
	private User testUser2;
	private Conversation testConversation;
	private ConversationDto testConversationDto;
	private CreateConversationRequest testCreateRequest;

	@BeforeEach
	void setUp() {
		testUserId1 = UUID.randomUUID();
		testUserId2 = UUID.randomUUID();
		testConversationId = UUID.randomUUID();

		testUser1 = aUser().withId(testUserId1).withName("Test User 1").build();
		testUser2 = aUser().withId(testUserId2).withName("Test User 2").build();

		testConversation = aConversation().withId(testConversationId).build();
		testConversationDto = aConversationDto().withId(testConversationId).build();

		testCreateRequest = new CreateConversationRequest(testUserId2);
	}

	@Nested
	@DisplayName("Create Conversation Tests")
	class CreateConversationTests {

		@Test
		@DisplayName("Should create conversation successfully when both users exist")
		void createConversation_ShouldReturnConversationDto_WhenBothUsersExist() {
			// Given
			when(userRepository.findById(testUserId1)).thenReturn(Optional.of(testUser1));
			when(userRepository.findById(testUserId2)).thenReturn(Optional.of(testUser2));
			when(conversationMapper.createNewConversation()).thenReturn(testConversation);
			when(conversationRepository.save(testConversation)).thenReturn(testConversation);
			when(conversationRepository.findById(testConversationId))
					.thenReturn(Optional.of(testConversation));
			when(conversationMapper.toDto(testConversation)).thenReturn(testConversationDto);

			// Mock participant operations
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							testConversationId, testUserId1))
					.thenReturn(false);
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							testConversationId, testUserId2))
					.thenReturn(false);

			// When
			ConversationDto result =
					conversationService.createConversation(testCreateRequest, testUserId1);

			// Then
			assertNotNull(result);
			assertEquals(testConversationId, result.getId());

			// Each user is looked up twice: once in createConversation and once in addParticipant
			verify(userRepository, times(2)).findById(testUserId1);
			verify(userRepository, times(2)).findById(testUserId2);
			verify(conversationMapper).createNewConversation();
			verify(conversationRepository).save(testConversation);
			verify(conversationParticipantRepository, times(2))
					.save(any(ConversationParticipant.class));
		}

		@Test
		@DisplayName("Should throw UserNotFoundException when creator does not exist")
		void createConversation_ShouldThrowException_WhenCreatorNotFound() {
			// Given
			when(userRepository.findById(testUserId1)).thenReturn(Optional.empty());

			// When & Then
			assertThrows(
					UserNotFoundException.class,
					() -> conversationService.createConversation(testCreateRequest, testUserId1));

			verify(userRepository).findById(testUserId1);
			verify(conversationRepository, never()).save(any());
		}

		@Test
		@DisplayName("Should throw UserNotFoundException when participant does not exist")
		void createConversation_ShouldThrowException_WhenParticipantNotFound() {
			// Given
			when(userRepository.findById(testUserId1)).thenReturn(Optional.of(testUser1));
			when(userRepository.findById(testUserId2)).thenReturn(Optional.empty());

			// When & Then
			assertThrows(
					UserNotFoundException.class,
					() -> conversationService.createConversation(testCreateRequest, testUserId1));

			verify(userRepository).findById(testUserId1);
			verify(userRepository).findById(testUserId2);
			verify(conversationRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("Add Participant Tests")
	class AddParticipantTests {

		@Test
		@DisplayName(
				"Should add participant successfully when user exists and not already participant")
		void addParticipant_ShouldAddParticipant_WhenUserExistsAndNotAlreadyParticipant() {
			// Given
			when(userRepository.findById(testUserId1)).thenReturn(Optional.of(testUser1));
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							testConversationId, testUserId1))
					.thenReturn(false);

			// When
			conversationService.addParticipant(testConversationId, testUserId1);

			// Then
			ArgumentCaptor<ConversationParticipant> participantCaptor =
					ArgumentCaptor.forClass(ConversationParticipant.class);
			verify(conversationParticipantRepository).save(participantCaptor.capture());

			ConversationParticipant savedParticipant = participantCaptor.getValue();
			assertEquals(testConversationId, savedParticipant.getConversationId());
			assertEquals(testUserId1, savedParticipant.getParticipantId());
			assertTrue(savedParticipant.isActive());
		}

		@Test
		@DisplayName("Should not add participant when user is already participant")
		void addParticipant_ShouldNotAddParticipant_WhenUserAlreadyParticipant() {
			// Given
			when(userRepository.findById(testUserId1)).thenReturn(Optional.of(testUser1));
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							testConversationId, testUserId1))
					.thenReturn(true);

			// When
			conversationService.addParticipant(testConversationId, testUserId1);

			// Then
			verify(conversationParticipantRepository, never())
					.save(any(ConversationParticipant.class));
		}

		@Test
		@DisplayName("Should throw UserNotFoundException when user does not exist")
		void addParticipant_ShouldThrowException_WhenUserNotFound() {
			// Given
			when(userRepository.findById(testUserId1)).thenReturn(Optional.empty());

			// When & Then
			assertThrows(
					UserNotFoundException.class,
					() -> conversationService.addParticipant(testConversationId, testUserId1));

			verify(conversationParticipantRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("Find User Conversations Tests")
	class FindUserConversationsTests {

		@Test
		@DisplayName("Should return list of conversations when user exists")
		void findUserConversations_ShouldReturnList_WhenUserExists() {
			// Given
			List<Conversation> conversations = Arrays.asList(testConversation);
			List<ConversationDto> conversationDtos = Arrays.asList(testConversationDto);

			when(userRepository.existsById(testUserId1)).thenReturn(true);
			when(conversationRepository.findByUserId(testUserId1)).thenReturn(conversations);
			when(conversationMapper.toDto(testConversation)).thenReturn(testConversationDto);

			// When
			List<ConversationDto> result = conversationService.findUserConversations(testUserId1);

			// Then
			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(testConversationId, result.get(0).getId());

			verify(userRepository).existsById(testUserId1);
			verify(conversationRepository).findByUserId(testUserId1);
		}

		@Test
		@DisplayName("Should throw UserNotFoundException when user does not exist")
		void findUserConversations_ShouldThrowException_WhenUserNotFound() {
			// Given
			when(userRepository.existsById(testUserId1)).thenReturn(false);

			// When & Then
			assertThrows(
					UserNotFoundException.class,
					() -> conversationService.findUserConversations(testUserId1));

			verify(conversationRepository, never()).findByUserId(any());
		}
	}

	@Nested
	@DisplayName("Find Conversation By ID and User Tests")
	class FindConversationByIdAndUserTests {

		@Test
		@DisplayName("Should return conversation when user is participant")
		void findConversationByIdAndUser_ShouldReturnConversation_WhenUserIsParticipant() {
			// Given
			when(userRepository.existsById(testUserId1)).thenReturn(true);
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							testConversationId, testUserId1))
					.thenReturn(true);
			when(conversationRepository.findById(testConversationId))
					.thenReturn(Optional.of(testConversation));
			when(conversationMapper.toDto(testConversation)).thenReturn(testConversationDto);

			// When
			ConversationDto result =
					conversationService.findConversationByIdAndUser(
							testConversationId, testUserId1);

			// Then
			assertNotNull(result);
			assertEquals(testConversationId, result.getId());

			verify(conversationRepository).findById(testConversationId);
			verify(conversationMapper).toDto(testConversation);
		}

		@Test
		@DisplayName("Should throw AccessDeniedException when user is not participant")
		void findConversationByIdAndUser_ShouldThrowException_WhenUserNotParticipant() {
			// Given
			when(userRepository.existsById(testUserId1)).thenReturn(true);
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							testConversationId, testUserId1))
					.thenReturn(false);

			// When & Then
			assertThrows(
					AccessDeniedException.class,
					() ->
							conversationService.findConversationByIdAndUser(
									testConversationId, testUserId1));

			verify(conversationRepository, never()).findById(any());
		}

		@Test
		@DisplayName("Should throw ConversationNotFoundException when conversation does not exist")
		void findConversationByIdAndUser_ShouldThrowException_WhenConversationNotFound() {
			// Given
			when(userRepository.existsById(testUserId1)).thenReturn(true);
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							testConversationId, testUserId1))
					.thenReturn(true);
			when(conversationRepository.findById(testConversationId)).thenReturn(Optional.empty());

			// When & Then
			assertThrows(
					ConversationNotFoundException.class,
					() ->
							conversationService.findConversationByIdAndUser(
									testConversationId, testUserId1));
		}
	}

	@Nested
	@DisplayName("Find Or Create Conversation Tests")
	class FindOrCreateConversationTests {

		@Test
		@DisplayName("Should return existing conversation when found")
		void findOrCreateConversation_ShouldReturnExisting_WhenConversationExists() {
			// Given
			when(userRepository.existsById(testUserId1)).thenReturn(true);
			when(userRepository.existsById(testUserId2)).thenReturn(true);
			when(conversationRepository.findDirectConversationBetweenUsers(
							testUserId1, testUserId2))
					.thenReturn(Optional.of(testConversation));
			when(conversationMapper.toDto(testConversation)).thenReturn(testConversationDto);

			// When
			ConversationDto result =
					conversationService.findOrCreateConversation(testCreateRequest, testUserId1);

			// Then
			assertNotNull(result);
			assertEquals(testConversationId, result.getId());

			verify(conversationRepository)
					.findDirectConversationBetweenUsers(testUserId1, testUserId2);
			verify(conversationRepository, never()).save(any());
		}

		@Test
		@DisplayName("Should create new conversation when not found")
		void findOrCreateConversation_ShouldCreateNew_WhenConversationNotExists() {
			// Given
			when(userRepository.existsById(testUserId1)).thenReturn(true);
			when(userRepository.existsById(testUserId2)).thenReturn(true);
			when(conversationRepository.findDirectConversationBetweenUsers(
							testUserId1, testUserId2))
					.thenReturn(Optional.empty());

			// Mock createConversation flow
			when(userRepository.findById(testUserId1)).thenReturn(Optional.of(testUser1));
			when(userRepository.findById(testUserId2)).thenReturn(Optional.of(testUser2));
			when(conversationMapper.createNewConversation()).thenReturn(testConversation);
			when(conversationRepository.save(testConversation)).thenReturn(testConversation);
			when(conversationRepository.findById(testConversationId))
					.thenReturn(Optional.of(testConversation));
			when(conversationMapper.toDto(testConversation)).thenReturn(testConversationDto);

			// Mock participant operations
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							testConversationId, testUserId1))
					.thenReturn(false);
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							testConversationId, testUserId2))
					.thenReturn(false);

			// When
			ConversationDto result =
					conversationService.findOrCreateConversation(testCreateRequest, testUserId1);

			// Then
			assertNotNull(result);
			assertEquals(testConversationId, result.getId());

			verify(conversationRepository)
					.findDirectConversationBetweenUsers(testUserId1, testUserId2);
			verify(conversationRepository).save(testConversation);
		}

		@Test
		@DisplayName("Should throw UserNotFoundException when creator does not exist")
		void findOrCreateConversation_ShouldThrowException_WhenCreatorNotFound() {
			// Given
			when(userRepository.existsById(testUserId1)).thenReturn(false);

			// When & Then
			assertThrows(
					UserNotFoundException.class,
					() ->
							conversationService.findOrCreateConversation(
									testCreateRequest, testUserId1));

			verify(conversationRepository, never())
					.findDirectConversationBetweenUsers(any(), any());
		}

		@Test
		@DisplayName("Should reactivate inactive participant when finding existing conversation")
		void
				findOrCreateConversation_ShouldReactivateInactiveParticipant_WhenUserWasPreviouslyInactive() {
			// Given
			when(userRepository.existsById(testUserId1)).thenReturn(true);
			when(userRepository.existsById(testUserId2)).thenReturn(true);

			// Create an inactive participant for the creator (testUserId1)
			ConversationParticipant inactiveCreatorParticipant =
					aConversationParticipant()
							.withConversationId(testConversationId)
							.withParticipantId(testUserId1)
							.withActive(false)
							.withLeftAt(LocalDateTime.now().minusDays(1))
							.build();

			ConversationParticipant activeOtherParticipant =
					aConversationParticipant()
							.withConversationId(testConversationId)
							.withParticipantId(testUserId2)
							.withActive(true)
							.build();

			// Set up the existing conversation with participants
			Conversation existingConversation =
					aConversation()
							.withId(testConversationId)
							.withParticipants(
									Arrays.asList(
											inactiveCreatorParticipant, activeOtherParticipant))
							.build();

			when(conversationRepository.findDirectConversationBetweenUsers(
							testUserId1, testUserId2))
					.thenReturn(Optional.of(existingConversation));

			// Mock the refetch after participant reactivation
			ConversationParticipant reactivatedParticipant =
					aConversationParticipant()
							.withConversationId(testConversationId)
							.withParticipantId(testUserId1)
							.withActive(true)
							.withJoinedAt(LocalDateTime.now())
							.withLeftAt(null)
							.build();

			Conversation updatedConversation =
					aConversation()
							.withId(testConversationId)
							.withParticipants(
									Arrays.asList(reactivatedParticipant, activeOtherParticipant))
							.build();

			when(conversationRepository.findById(testConversationId))
					.thenReturn(Optional.of(updatedConversation));
			when(conversationMapper.toDto(updatedConversation)).thenReturn(testConversationDto);

			// When
			ConversationDto result =
					conversationService.findOrCreateConversation(testCreateRequest, testUserId1);

			// Then
			assertNotNull(result);
			assertEquals(testConversationId, result.getId());

			// Verify that the participant was reactivated
			ArgumentCaptor<ConversationParticipant> participantCaptor =
					ArgumentCaptor.forClass(ConversationParticipant.class);
			verify(conversationParticipantRepository).save(participantCaptor.capture());

			ConversationParticipant savedParticipant = participantCaptor.getValue();
			assertTrue(savedParticipant.isActive());
			assertNotNull(savedParticipant.getJoinedAt());
			assertNull(savedParticipant.getLeftAt());
			assertEquals(testUserId1, savedParticipant.getParticipantId());

			// Verify conversation was refetched after participant update
			verify(conversationRepository).findById(testConversationId);
			verify(conversationMapper).toDto(updatedConversation);
		}

		@Test
		@DisplayName(
				"Should not reactivate participant when already active in existing conversation")
		void findOrCreateConversation_ShouldNotReactivateParticipant_WhenUserIsAlreadyActive() {
			// Given
			when(userRepository.existsById(testUserId1)).thenReturn(true);
			when(userRepository.existsById(testUserId2)).thenReturn(true);

			// Create active participants
			ConversationParticipant activeCreatorParticipant =
					aConversationParticipant()
							.withConversationId(testConversationId)
							.withParticipantId(testUserId1)
							.withActive(true)
							.build();

			ConversationParticipant activeOtherParticipant =
					aConversationParticipant()
							.withConversationId(testConversationId)
							.withParticipantId(testUserId2)
							.withActive(true)
							.build();

			// Set up the existing conversation with active participants
			Conversation existingConversation =
					aConversation()
							.withId(testConversationId)
							.withParticipants(
									Arrays.asList(activeCreatorParticipant, activeOtherParticipant))
							.build();

			when(conversationRepository.findDirectConversationBetweenUsers(
							testUserId1, testUserId2))
					.thenReturn(Optional.of(existingConversation));
			when(conversationMapper.toDto(existingConversation)).thenReturn(testConversationDto);

			// When
			ConversationDto result =
					conversationService.findOrCreateConversation(testCreateRequest, testUserId1);

			// Then
			assertNotNull(result);
			assertEquals(testConversationId, result.getId());

			// Verify that no participant was saved (no reactivation needed)
			verify(conversationParticipantRepository, never()).save(any());

			// Verify conversation was not refetched
			verify(conversationRepository, never()).findById(testConversationId);
			verify(conversationMapper).toDto(existingConversation);
		}
	}

	@Nested
	@DisplayName("Delete Conversation For User Tests")
	class DeleteConversationForUserTests {

		@Test
		@DisplayName("Should mark participant as inactive when user is participant")
		void deleteConversationForUser_ShouldMarkInactive_WhenUserIsParticipant() {
			// Given
			ConversationParticipant participant =
					aConversationParticipant()
							.withConversationId(testConversationId)
							.withParticipantId(testUserId1)
							.withActive(true)
							.build();

			when(conversationParticipantRepository.findByConversationIdAndUserIdAndIsActiveTrue(
							testConversationId, testUserId1))
					.thenReturn(Optional.of(participant));
			when(conversationParticipantRepository.findByConversationId(testConversationId))
					.thenReturn(Arrays.asList(participant));

			// When
			conversationService.deleteConversationForUser(testConversationId, testUserId1);

			// Then
			ArgumentCaptor<ConversationParticipant> participantCaptor =
					ArgumentCaptor.forClass(ConversationParticipant.class);
			verify(conversationParticipantRepository).save(participantCaptor.capture());

			ConversationParticipant savedParticipant = participantCaptor.getValue();
			assertFalse(savedParticipant.isActive());
			assertNotNull(savedParticipant.getLeftAt());
		}

		@Test
		@DisplayName("Should throw AccessDeniedException when user is not participant")
		void deleteConversationForUser_ShouldThrowException_WhenUserNotParticipant() {
			// Given
			when(conversationParticipantRepository.findByConversationIdAndUserIdAndIsActiveTrue(
							testConversationId, testUserId1))
					.thenReturn(Optional.empty());

			// When & Then
			assertThrows(
					AccessDeniedException.class,
					() ->
							conversationService.deleteConversationForUser(
									testConversationId, testUserId1));

			verify(conversationParticipantRepository, never()).save(any());
			verify(conversationRepository, never()).delete(any());
		}
	}

	@Nested
	@DisplayName("Mark Conversation As Read Tests")
	class MarkConversationAsReadTests {

		@Test
		@DisplayName("Should update last read time and publish event when user is participant")
		void markConversationAsRead_ShouldUpdateLastReadTime_WhenUserIsParticipant() {
			// Given
			ConversationParticipant participant =
					aConversationParticipant()
							.withConversationId(testConversationId)
							.withParticipantId(testUserId1)
							.withActive(true)
							.build();

			when(conversationParticipantRepository.findByConversationIdAndUserIdAndIsActiveTrue(
							testConversationId, testUserId1))
					.thenReturn(Optional.of(participant));
			when(conversationRepository.findById(testConversationId))
					.thenReturn(Optional.of(testConversation));
			when(conversationMapper.toDto(testConversation)).thenReturn(testConversationDto);

			// When
			conversationService.markConversationAsRead(testConversationId, testUserId1);

			// Then
			ArgumentCaptor<ConversationParticipant> participantCaptor =
					ArgumentCaptor.forClass(ConversationParticipant.class);
			verify(conversationParticipantRepository).save(participantCaptor.capture());

			ConversationParticipant savedParticipant = participantCaptor.getValue();
			assertNotNull(savedParticipant.getLastReadAt());

			verify(applicationEventPublisher).publishEvent(any(ConversationUpdateEvent.class));
		}

		@Test
		@DisplayName("Should throw AccessDeniedException when user is not participant")
		void markConversationAsRead_ShouldThrowException_WhenUserNotParticipant() {
			// Given
			when(conversationParticipantRepository.findByConversationIdAndUserIdAndIsActiveTrue(
							testConversationId, testUserId1))
					.thenReturn(Optional.empty());

			// When & Then
			assertThrows(
					AccessDeniedException.class,
					() ->
							conversationService.markConversationAsRead(
									testConversationId, testUserId1));

			verify(conversationParticipantRepository, never()).save(any());
			verify(applicationEventPublisher, never()).publishEvent(any());
		}
	}
}
