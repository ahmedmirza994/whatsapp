/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import com.ah.whatsapp.dto.ApiResponse;
import com.ah.whatsapp.dto.ConversationDto;
import com.ah.whatsapp.dto.ConversationDtoTestDataBuilder;
import com.ah.whatsapp.dto.CreateConversationRequest;
import com.ah.whatsapp.dto.CreateConversationRequestTestDataBuilder;
import com.ah.whatsapp.dto.MessageDtoTestDataBuilder;
import com.ah.whatsapp.dto.ParticipantDtoTestDataBuilder;
import com.ah.whatsapp.exception.ConversationNotFoundException;
import com.ah.whatsapp.exception.UserNotFoundException;
import com.ah.whatsapp.model.JwtUser;
import com.ah.whatsapp.service.ConversationService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationController Unit Tests")
class ConversationControllerTest {

	@Mock private ConversationService conversationService;

	@InjectMocks private ConversationController conversationController;

	// Test data constants
	private static final UUID TEST_USER_ID = UUID.randomUUID();
	private static final UUID TEST_CONVERSATION_ID = UUID.randomUUID();
	private static final UUID PARTICIPANT_ID = UUID.randomUUID();
	private static final UUID OTHER_USER_ID = UUID.randomUUID();
	private static final String TEST_EMAIL = "test@example.com";
	private static final String PARTICIPANT_EMAIL = "participant@example.com";
	private static final String PARTICIPANT_NAME = "Participant User";

	@Nested
	@DisplayName("Get User Conversations - Success Cases")
	class GetUserConversationsSuccessCases {

		@Test
		@DisplayName("Should successfully return user conversations")
		void shouldReturnUserConversationsSuccessfully() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			List<ConversationDto> expectedConversations =
					List.of(
							ConversationDtoTestDataBuilder.aConversationDto()
									.withId(TEST_CONVERSATION_ID)
									.withParticipants(
											List.of(
													ParticipantDtoTestDataBuilder.aParticipantDto()
															.withUserId(TEST_USER_ID)
															.withEmail(TEST_EMAIL)
															.build(),
													ParticipantDtoTestDataBuilder.aParticipantDto()
															.withUserId(PARTICIPANT_ID)
															.withEmail(PARTICIPANT_EMAIL)
															.withName(PARTICIPANT_NAME)
															.build()))
									.withLastMessage(
											MessageDtoTestDataBuilder.aMessageDto()
													.withContent("Hello there!")
													.withSentAt(LocalDateTime.now())
													.build())
									.build(),
							ConversationDtoTestDataBuilder.aConversationDto()
									.withId(UUID.randomUUID())
									.withParticipants(
											List.of(
													ParticipantDtoTestDataBuilder.aParticipantDto()
															.withUserId(TEST_USER_ID)
															.build(),
													ParticipantDtoTestDataBuilder.aParticipantDto()
															.withUserId(OTHER_USER_ID)
															.build()))
									.build());

			when(conversationService.findUserConversations(TEST_USER_ID))
					.thenReturn(expectedConversations);

			// When
			ResponseEntity<ApiResponse<List<ConversationDto>>> response =
					conversationController.getUserConversations(currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(200, response.getBody().getStatus());
			assertNotNull(response.getBody().getData());
			assertEquals(2, response.getBody().getData().size());
			assertEquals(TEST_CONVERSATION_ID, response.getBody().getData().get(0).getId());

			verify(conversationService).findUserConversations(TEST_USER_ID);
		}

		@Test
		@DisplayName("Should return empty list when user has no conversations")
		void shouldReturnEmptyListWhenUserHasNoConversations() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			List<ConversationDto> emptyConversations = Collections.emptyList();

			when(conversationService.findUserConversations(TEST_USER_ID))
					.thenReturn(emptyConversations);

			// When
			ResponseEntity<ApiResponse<List<ConversationDto>>> response =
					conversationController.getUserConversations(currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(200, response.getBody().getStatus());
			assertNotNull(response.getBody().getData());
			assertEquals(0, response.getBody().getData().size());

			verify(conversationService).findUserConversations(TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Get User Conversations - Error Cases")
	class GetUserConversationsErrorCases {

		@Test
		@DisplayName("Should handle service exception when getting conversations")
		void shouldHandleServiceExceptionWhenGettingConversations() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(conversationService.findUserConversations(TEST_USER_ID))
					.thenThrow(new RuntimeException("Database error"));

			// When & Then
			assertThrows(
					RuntimeException.class,
					() -> conversationController.getUserConversations(currentUser));

			verify(conversationService).findUserConversations(TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Create Conversation - Success Cases")
	class CreateConversationSuccessCases {

		@Test
		@DisplayName("Should successfully create a new conversation")
		void shouldCreateConversationSuccessfully() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			CreateConversationRequest request =
					CreateConversationRequestTestDataBuilder.aCreateConversationRequest()
							.withParticipantId(PARTICIPANT_ID)
							.build();

			ConversationDto expectedConversation =
					ConversationDtoTestDataBuilder.aConversationDto()
							.withId(TEST_CONVERSATION_ID)
							.withParticipants(
									List.of(
											ParticipantDtoTestDataBuilder.aParticipantDto()
													.withUserId(TEST_USER_ID)
													.withEmail(TEST_EMAIL)
													.build(),
											ParticipantDtoTestDataBuilder.aParticipantDto()
													.withUserId(PARTICIPANT_ID)
													.withEmail(PARTICIPANT_EMAIL)
													.withName(PARTICIPANT_NAME)
													.build()))
							.build();

			when(conversationService.createConversation(request, TEST_USER_ID))
					.thenReturn(expectedConversation);

			// When
			ResponseEntity<ApiResponse<ConversationDto>> response =
					conversationController.createConversation(request, currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.CREATED, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(200, response.getBody().getStatus());
			assertNotNull(response.getBody().getData());
			assertEquals(TEST_CONVERSATION_ID, response.getBody().getData().getId());
			assertEquals(2, response.getBody().getData().getParticipants().size());

			verify(conversationService).createConversation(request, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Create Conversation - Error Cases")
	class CreateConversationErrorCases {

		@Test
		@DisplayName("Should handle user not found when creating conversation")
		void shouldHandleUserNotFoundWhenCreatingConversation() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			CreateConversationRequest request =
					CreateConversationRequestTestDataBuilder.aCreateConversationRequest()
							.withParticipantId(PARTICIPANT_ID)
							.build();

			when(conversationService.createConversation(request, TEST_USER_ID))
					.thenThrow(new UserNotFoundException("Participant user not found"));

			// When & Then
			assertThrows(
					UserNotFoundException.class,
					() -> conversationController.createConversation(request, currentUser));

			verify(conversationService).createConversation(request, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle general exception when creating conversation")
		void shouldHandleGeneralExceptionWhenCreatingConversation() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			CreateConversationRequest request =
					CreateConversationRequestTestDataBuilder.aCreateConversationRequest()
							.withParticipantId(PARTICIPANT_ID)
							.build();

			when(conversationService.createConversation(request, TEST_USER_ID))
					.thenThrow(new RuntimeException("Database error"));

			// When & Then
			assertThrows(
					RuntimeException.class,
					() -> conversationController.createConversation(request, currentUser));

			verify(conversationService).createConversation(request, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Get Conversation By ID - Success Cases")
	class GetConversationByIdSuccessCases {

		@Test
		@DisplayName("Should successfully return conversation by ID")
		void shouldReturnConversationByIdSuccessfully() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			ConversationDto expectedConversation =
					ConversationDtoTestDataBuilder.aConversationDto()
							.withId(TEST_CONVERSATION_ID)
							.withParticipants(
									List.of(
											ParticipantDtoTestDataBuilder.aParticipantDto()
													.withUserId(TEST_USER_ID)
													.withEmail(TEST_EMAIL)
													.build(),
											ParticipantDtoTestDataBuilder.aParticipantDto()
													.withUserId(PARTICIPANT_ID)
													.withEmail(PARTICIPANT_EMAIL)
													.withName(PARTICIPANT_NAME)
													.build()))
							.withMessages(
									List.of(
											MessageDtoTestDataBuilder.aMessageDto()
													.withContent("Hello!")
													.withSenderId(TEST_USER_ID)
													.withConversationId(TEST_CONVERSATION_ID)
													.build()))
							.build();

			when(conversationService.findConversationByIdAndUser(
							TEST_CONVERSATION_ID, TEST_USER_ID))
					.thenReturn(expectedConversation);

			// When
			ResponseEntity<ApiResponse<ConversationDto>> response =
					conversationController.getConversationById(TEST_CONVERSATION_ID, currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(200, response.getBody().getStatus());
			assertNotNull(response.getBody().getData());
			assertEquals(TEST_CONVERSATION_ID, response.getBody().getData().getId());
			assertEquals(2, response.getBody().getData().getParticipants().size());
			assertEquals(1, response.getBody().getData().getMessages().size());

			verify(conversationService)
					.findConversationByIdAndUser(TEST_CONVERSATION_ID, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Get Conversation By ID - Error Cases")
	class GetConversationByIdErrorCases {

		@Test
		@DisplayName("Should handle conversation not found")
		void shouldHandleConversationNotFound() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(conversationService.findConversationByIdAndUser(
							TEST_CONVERSATION_ID, TEST_USER_ID))
					.thenThrow(new ConversationNotFoundException("Conversation not found"));

			// When & Then
			assertThrows(
					ConversationNotFoundException.class,
					() ->
							conversationController.getConversationById(
									TEST_CONVERSATION_ID, currentUser));

			verify(conversationService)
					.findConversationByIdAndUser(TEST_CONVERSATION_ID, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle access denied")
		void shouldHandleAccessDenied() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(conversationService.findConversationByIdAndUser(
							TEST_CONVERSATION_ID, TEST_USER_ID))
					.thenThrow(new AccessDeniedException("Access denied"));

			// When & Then
			assertThrows(
					AccessDeniedException.class,
					() ->
							conversationController.getConversationById(
									TEST_CONVERSATION_ID, currentUser));

			verify(conversationService)
					.findConversationByIdAndUser(TEST_CONVERSATION_ID, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Find or Create Conversation - Success Cases")
	class FindOrCreateConversationSuccessCases {

		@Test
		@DisplayName("Should find existing conversation")
		void shouldFindExistingConversation() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			CreateConversationRequest request =
					CreateConversationRequestTestDataBuilder.aCreateConversationRequest()
							.withParticipantId(PARTICIPANT_ID)
							.build();

			ConversationDto existingConversation =
					ConversationDtoTestDataBuilder.aConversationDto()
							.withId(TEST_CONVERSATION_ID)
							.withParticipants(
									List.of(
											ParticipantDtoTestDataBuilder.aParticipantDto()
													.withUserId(TEST_USER_ID)
													.build(),
											ParticipantDtoTestDataBuilder.aParticipantDto()
													.withUserId(PARTICIPANT_ID)
													.build()))
							.build();

			when(conversationService.findOrCreateConversation(request, TEST_USER_ID))
					.thenReturn(existingConversation);

			// When
			ResponseEntity<ApiResponse<ConversationDto>> response =
					conversationController.findOrCreateDirectConversation(request, currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.CREATED, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(200, response.getBody().getStatus());
			assertNotNull(response.getBody().getData());
			assertEquals(TEST_CONVERSATION_ID, response.getBody().getData().getId());

			verify(conversationService).findOrCreateConversation(request, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should create new conversation when not found")
		void shouldCreateNewConversationWhenNotFound() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			CreateConversationRequest request =
					CreateConversationRequestTestDataBuilder.aCreateConversationRequest()
							.withParticipantId(PARTICIPANT_ID)
							.build();

			ConversationDto newConversation =
					ConversationDtoTestDataBuilder.aConversationDto()
							.withId(UUID.randomUUID())
							.withParticipants(
									List.of(
											ParticipantDtoTestDataBuilder.aParticipantDto()
													.withUserId(TEST_USER_ID)
													.build(),
											ParticipantDtoTestDataBuilder.aParticipantDto()
													.withUserId(PARTICIPANT_ID)
													.build()))
							.build();

			when(conversationService.findOrCreateConversation(request, TEST_USER_ID))
					.thenReturn(newConversation);

			// When
			ResponseEntity<ApiResponse<ConversationDto>> response =
					conversationController.findOrCreateDirectConversation(request, currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.CREATED, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(200, response.getBody().getStatus());
			assertNotNull(response.getBody().getData());
			assertEquals(2, response.getBody().getData().getParticipants().size());

			verify(conversationService).findOrCreateConversation(request, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Find or Create Conversation - Error Cases")
	class FindOrCreateConversationErrorCases {

		@Test
		@DisplayName("Should handle user not found when finding or creating conversation")
		void shouldHandleUserNotFoundWhenFindingOrCreatingConversation() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			CreateConversationRequest request =
					CreateConversationRequestTestDataBuilder.aCreateConversationRequest()
							.withParticipantId(PARTICIPANT_ID)
							.build();

			when(conversationService.findOrCreateConversation(request, TEST_USER_ID))
					.thenThrow(new UserNotFoundException("Participant user not found"));

			// When & Then
			assertThrows(
					UserNotFoundException.class,
					() ->
							conversationController.findOrCreateDirectConversation(
									request, currentUser));

			verify(conversationService).findOrCreateConversation(request, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Delete Conversation - Success Cases")
	class DeleteConversationSuccessCases {

		@Test
		@DisplayName("Should successfully delete conversation")
		void shouldDeleteConversationSuccessfully() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			// When
			ResponseEntity<ApiResponse<UUID>> response =
					conversationController.deleteConversation(TEST_CONVERSATION_ID, currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(200, response.getBody().getStatus());

			verify(conversationService)
					.deleteConversationForUser(TEST_CONVERSATION_ID, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Delete Conversation - Error Cases")
	class DeleteConversationErrorCases {

		@Test
		@DisplayName("Should handle conversation not found when deleting")
		void shouldHandleConversationNotFoundWhenDeleting() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			doThrow(new ConversationNotFoundException("Conversation not found"))
					.when(conversationService)
					.deleteConversationForUser(TEST_CONVERSATION_ID, TEST_USER_ID);

			// When & Then
			assertThrows(
					ConversationNotFoundException.class,
					() ->
							conversationController.deleteConversation(
									TEST_CONVERSATION_ID, currentUser));

			verify(conversationService)
					.deleteConversationForUser(TEST_CONVERSATION_ID, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle access denied when deleting")
		void shouldHandleAccessDeniedWhenDeleting() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			doThrow(new AccessDeniedException("Access denied"))
					.when(conversationService)
					.deleteConversationForUser(TEST_CONVERSATION_ID, TEST_USER_ID);

			// When & Then
			assertThrows(
					AccessDeniedException.class,
					() ->
							conversationController.deleteConversation(
									TEST_CONVERSATION_ID, currentUser));

			verify(conversationService)
					.deleteConversationForUser(TEST_CONVERSATION_ID, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Mark Conversation as Read - Success Cases")
	class MarkConversationAsReadSuccessCases {

		@Test
		@DisplayName("Should successfully mark conversation as read")
		void shouldMarkConversationAsReadSuccessfully() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			// When
			ResponseEntity<ApiResponse<UUID>> response =
					conversationController.markAsRead(TEST_CONVERSATION_ID, currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(200, response.getBody().getStatus());

			verify(conversationService).markConversationAsRead(TEST_CONVERSATION_ID, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Mark Conversation as Read - Error Cases")
	class MarkConversationAsReadErrorCases {

		@Test
		@DisplayName("Should handle conversation not found when marking as read")
		void shouldHandleConversationNotFoundWhenMarkingAsRead() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			doThrow(new ConversationNotFoundException("Conversation not found"))
					.when(conversationService)
					.markConversationAsRead(TEST_CONVERSATION_ID, TEST_USER_ID);

			// When & Then
			assertThrows(
					ConversationNotFoundException.class,
					() -> conversationController.markAsRead(TEST_CONVERSATION_ID, currentUser));

			verify(conversationService).markConversationAsRead(TEST_CONVERSATION_ID, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle access denied when marking as read")
		void shouldHandleAccessDeniedWhenMarkingAsRead() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			doThrow(new AccessDeniedException("Access denied"))
					.when(conversationService)
					.markConversationAsRead(TEST_CONVERSATION_ID, TEST_USER_ID);

			// When & Then
			assertThrows(
					AccessDeniedException.class,
					() -> conversationController.markAsRead(TEST_CONVERSATION_ID, currentUser));

			verify(conversationService).markConversationAsRead(TEST_CONVERSATION_ID, TEST_USER_ID);
		}
	}
}
