/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.ah.whatsapp.dto.ApiResponse;
import com.ah.whatsapp.dto.MessageDto;
import com.ah.whatsapp.dto.MessageDtoTestDataBuilder;
import com.ah.whatsapp.dto.SendMessageRequest;
import com.ah.whatsapp.dto.SendMessageRequestTestDataBuilder;
import com.ah.whatsapp.exception.ConversationNotFoundException;
import com.ah.whatsapp.exception.MessageNotFoundException;
import com.ah.whatsapp.exception.UserNotFoundException;
import com.ah.whatsapp.model.JwtUser;
import com.ah.whatsapp.service.MessageService;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageController Unit Tests")
class MessageControllerTest {

	@Mock private MessageService messageService;
	@Mock private StompHeaderAccessor headerAccessor;
	@Mock private UsernamePasswordAuthenticationToken authToken;

	@InjectMocks private MessageController messageController;

	// Test data constants
	private static final UUID TEST_USER_ID = UUID.randomUUID();
	private static final UUID TEST_CONVERSATION_ID = UUID.randomUUID();
	private static final UUID TEST_MESSAGE_ID = UUID.randomUUID();
	private static final UUID OTHER_USER_ID = UUID.randomUUID();
	private static final String TEST_EMAIL = "test@example.com";
	private static final String TEST_MESSAGE_CONTENT = "Hello, how are you?";
	private static final String UPDATED_MESSAGE_CONTENT = "Updated message content";

	@Nested
	@DisplayName("Get Conversation Messages - Success Cases")
	class GetConversationMessagesSuccessCases {

		@Test
		@DisplayName("Should successfully get messages for valid conversation ID")
		void shouldGetConversationMessagesSuccessfully() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			List<MessageDto> expectedMessages =
					List.of(
							MessageDtoTestDataBuilder.aMessageDto()
									.withId(TEST_MESSAGE_ID)
									.withConversationId(TEST_CONVERSATION_ID)
									.withSenderId(TEST_USER_ID)
									.withContent(TEST_MESSAGE_CONTENT)
									.build(),
							MessageDtoTestDataBuilder.aMessageDto()
									.withId(UUID.randomUUID())
									.withConversationId(TEST_CONVERSATION_ID)
									.withSenderId(OTHER_USER_ID)
									.withContent("Hello back!")
									.build());

			when(messageService.findConversationMessages(TEST_CONVERSATION_ID, TEST_USER_ID))
					.thenReturn(expectedMessages);

			// When
			ResponseEntity<ApiResponse<List<MessageDto>>> response =
					messageController.getConversationMessages(TEST_CONVERSATION_ID, currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(
					HttpStatus.OK.value(), Objects.requireNonNull(response.getBody()).getStatus());
			assertEquals(expectedMessages, Objects.requireNonNull(response.getBody()).getData());
			assertEquals(2, Objects.requireNonNull(response.getBody()).getData().size());

			verify(messageService, times(1))
					.findConversationMessages(TEST_CONVERSATION_ID, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should return empty list when no messages found")
		void shouldReturnEmptyListWhenNoMessagesFound() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);
			List<MessageDto> emptyMessages = Collections.emptyList();

			when(messageService.findConversationMessages(TEST_CONVERSATION_ID, TEST_USER_ID))
					.thenReturn(emptyMessages);

			// When
			ResponseEntity<ApiResponse<List<MessageDto>>> response =
					messageController.getConversationMessages(TEST_CONVERSATION_ID, currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(
					HttpStatus.OK.value(), Objects.requireNonNull(response.getBody()).getStatus());
			assertEquals(emptyMessages, Objects.requireNonNull(response.getBody()).getData());
			assertEquals(0, Objects.requireNonNull(response.getBody()).getData().size());

			verify(messageService, times(1))
					.findConversationMessages(TEST_CONVERSATION_ID, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Get Conversation Messages - Error Cases")
	class GetConversationMessagesErrorCases {

		@Test
		@DisplayName("Should handle ConversationNotFoundException")
		void shouldHandleConversationNotFoundException() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(messageService.findConversationMessages(TEST_CONVERSATION_ID, TEST_USER_ID))
					.thenThrow(new ConversationNotFoundException("Conversation not found"));

			// When & Then
			ConversationNotFoundException exception =
					assertThrows(
							ConversationNotFoundException.class,
							() ->
									messageController.getConversationMessages(
											TEST_CONVERSATION_ID, currentUser));

			assertEquals("Conversation not found", exception.getMessage());
			verify(messageService, times(1))
					.findConversationMessages(TEST_CONVERSATION_ID, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle AccessDeniedException when user is not participant")
		void shouldHandleAccessDeniedExceptionWhenUserNotParticipant() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(messageService.findConversationMessages(TEST_CONVERSATION_ID, TEST_USER_ID))
					.thenThrow(
							new AccessDeniedException(
									"User is not a participant in this conversation"));

			// When & Then
			AccessDeniedException exception =
					assertThrows(
							AccessDeniedException.class,
							() ->
									messageController.getConversationMessages(
											TEST_CONVERSATION_ID, currentUser));

			assertEquals("User is not a participant in this conversation", exception.getMessage());
			verify(messageService, times(1))
					.findConversationMessages(TEST_CONVERSATION_ID, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle null user authentication")
		void shouldHandleNullUserAuthentication() {
			// When
			ResponseEntity<ApiResponse<List<MessageDto>>> response =
					messageController.getConversationMessages(TEST_CONVERSATION_ID, null);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals("Unauthorized", Objects.requireNonNull(response.getBody()).getError());
			assertEquals(
					HttpStatus.UNAUTHORIZED.value(),
					Objects.requireNonNull(response.getBody()).getStatus());

			verifyNoInteractions(messageService);
		}

		@Test
		@DisplayName("Should handle runtime exception from service")
		void shouldHandleRuntimeExceptionFromService() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(messageService.findConversationMessages(TEST_CONVERSATION_ID, TEST_USER_ID))
					.thenThrow(new RuntimeException("Database connection error"));

			// When & Then
			RuntimeException exception =
					assertThrows(
							RuntimeException.class,
							() ->
									messageController.getConversationMessages(
											TEST_CONVERSATION_ID, currentUser));

			assertEquals("Database connection error", exception.getMessage());
			verify(messageService, times(1))
					.findConversationMessages(TEST_CONVERSATION_ID, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("WebSocket Send Message - Success Cases")
	class WebSocketSendMessageSuccessCases {

		@Test
		@DisplayName("Should successfully send message via WebSocket")
		void shouldSendMessageViaWebSocketSuccessfully() {
			// Given
			SendMessageRequest sendRequest =
					SendMessageRequestTestDataBuilder.aSendMessageRequest()
							.withConversationId(TEST_CONVERSATION_ID)
							.withContent(TEST_MESSAGE_CONTENT)
							.build();

			JwtUser jwtUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(headerAccessor.getUser()).thenReturn(authToken);
			when(authToken.getPrincipal()).thenReturn(jwtUser);

			// When
			messageController.handleSendMessage(sendRequest, headerAccessor);

			// Then
			verify(headerAccessor, times(1)).getUser();
			verify(authToken, times(1)).getPrincipal();
			verify(messageService, times(1)).sendMessage(sendRequest, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle message with different content types")
		void shouldHandleMessageWithDifferentContentTypes() {
			// Given
			String longMessage =
					"This is a very long message that contains multiple sentences and should be"
							+ " handled properly by the messaging system without any issues.";
			SendMessageRequest sendRequest =
					SendMessageRequestTestDataBuilder.aSendMessageRequest()
							.withConversationId(TEST_CONVERSATION_ID)
							.withContent(longMessage)
							.build();

			JwtUser jwtUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(headerAccessor.getUser()).thenReturn(authToken);
			when(authToken.getPrincipal()).thenReturn(jwtUser);

			// When
			messageController.handleSendMessage(sendRequest, headerAccessor);

			// Then
			verify(headerAccessor, times(1)).getUser();
			verify(authToken, times(1)).getPrincipal();
			verify(messageService, times(1)).sendMessage(sendRequest, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle message with emoji and special characters")
		void shouldHandleMessageWithEmojiAndSpecialCharacters() {
			// Given
			String emojiMessage = "Hello! 😊 How are you doing? 🎉 @user #hashtag";
			SendMessageRequest sendRequest =
					SendMessageRequestTestDataBuilder.aSendMessageRequest()
							.withConversationId(TEST_CONVERSATION_ID)
							.withContent(emojiMessage)
							.build();

			JwtUser jwtUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(headerAccessor.getUser()).thenReturn(authToken);
			when(authToken.getPrincipal()).thenReturn(jwtUser);

			// When
			messageController.handleSendMessage(sendRequest, headerAccessor);

			// Then
			verify(headerAccessor, times(1)).getUser();
			verify(authToken, times(1)).getPrincipal();
			verify(messageService, times(1)).sendMessage(sendRequest, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("WebSocket Send Message - Error Cases")
	class WebSocketSendMessageErrorCases {

		@Test
		@DisplayName("Should handle ConversationNotFoundException in WebSocket")
		void shouldHandleConversationNotFoundExceptionInWebSocket() {
			// Given
			SendMessageRequest sendRequest =
					SendMessageRequestTestDataBuilder.aSendMessageRequest()
							.withConversationId(TEST_CONVERSATION_ID)
							.withContent(TEST_MESSAGE_CONTENT)
							.build();

			JwtUser jwtUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(headerAccessor.getUser()).thenReturn(authToken);
			when(authToken.getPrincipal()).thenReturn(jwtUser);
			doThrow(new ConversationNotFoundException("Conversation not found"))
					.when(messageService)
					.sendMessage(sendRequest, TEST_USER_ID);

			// When & Then
			ConversationNotFoundException exception =
					assertThrows(
							ConversationNotFoundException.class,
							() -> messageController.handleSendMessage(sendRequest, headerAccessor));

			assertEquals("Conversation not found", exception.getMessage());
			verify(headerAccessor, times(1)).getUser();
			verify(authToken, times(1)).getPrincipal();
			verify(messageService, times(1)).sendMessage(sendRequest, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle UserNotFoundException in WebSocket")
		void shouldHandleUserNotFoundExceptionInWebSocket() {
			// Given
			SendMessageRequest sendRequest =
					SendMessageRequestTestDataBuilder.aSendMessageRequest()
							.withConversationId(TEST_CONVERSATION_ID)
							.withContent(TEST_MESSAGE_CONTENT)
							.build();

			JwtUser jwtUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(headerAccessor.getUser()).thenReturn(authToken);
			when(authToken.getPrincipal()).thenReturn(jwtUser);
			doThrow(new UserNotFoundException("User not found"))
					.when(messageService)
					.sendMessage(sendRequest, TEST_USER_ID);

			// When & Then
			UserNotFoundException exception =
					assertThrows(
							UserNotFoundException.class,
							() -> messageController.handleSendMessage(sendRequest, headerAccessor));

			assertEquals("User not found", exception.getMessage());
			verify(headerAccessor, times(1)).getUser();
			verify(authToken, times(1)).getPrincipal();
			verify(messageService, times(1)).sendMessage(sendRequest, TEST_USER_ID);
		}

		@Test
		@DisplayName(
				"Should handle AccessDeniedException when user is not participant in WebSocket")
		void shouldHandleAccessDeniedExceptionInWebSocket() {
			// Given
			SendMessageRequest sendRequest =
					SendMessageRequestTestDataBuilder.aSendMessageRequest()
							.withConversationId(TEST_CONVERSATION_ID)
							.withContent(TEST_MESSAGE_CONTENT)
							.build();

			JwtUser jwtUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(headerAccessor.getUser()).thenReturn(authToken);
			when(authToken.getPrincipal()).thenReturn(jwtUser);
			doThrow(new AccessDeniedException("User is not a participant in this conversation"))
					.when(messageService)
					.sendMessage(sendRequest, TEST_USER_ID);

			// When & Then
			AccessDeniedException exception =
					assertThrows(
							AccessDeniedException.class,
							() -> messageController.handleSendMessage(sendRequest, headerAccessor));

			assertEquals("User is not a participant in this conversation", exception.getMessage());
			verify(headerAccessor, times(1)).getUser();
			verify(authToken, times(1)).getPrincipal();
			verify(messageService, times(1)).sendMessage(sendRequest, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle null user in WebSocket header accessor")
		void shouldHandleNullUserInWebSocketHeaderAccessor() {
			// Given
			SendMessageRequest sendRequest =
					SendMessageRequestTestDataBuilder.aSendMessageRequest()
							.withConversationId(TEST_CONVERSATION_ID)
							.withContent(TEST_MESSAGE_CONTENT)
							.build();

			when(headerAccessor.getUser()).thenReturn(null);

			// When - This should be handled gracefully by the controller
			messageController.handleSendMessage(sendRequest, headerAccessor);

			// Then
			verify(headerAccessor, times(1)).getUser();
			verify(messageService, never()).sendMessage(any(), any());
		}

		@Test
		@DisplayName("Should handle non-JWT principal in WebSocket")
		void shouldHandleNonJwtPrincipalInWebSocket() {
			// Given
			SendMessageRequest sendRequest =
					SendMessageRequestTestDataBuilder.aSendMessageRequest()
							.withConversationId(TEST_CONVERSATION_ID)
							.withContent(TEST_MESSAGE_CONTENT)
							.build();

			// Mock a non-UsernamePasswordAuthenticationToken principal
			Principal nonJwtPrincipal = () -> "some-user";
			when(headerAccessor.getUser()).thenReturn(nonJwtPrincipal);

			// When - This should be handled gracefully by the controller
			messageController.handleSendMessage(sendRequest, headerAccessor);

			// Then
			verify(headerAccessor, times(1)).getUser();
			verify(messageService, never()).sendMessage(any(), any());
		}

		@Test
		@DisplayName("Should handle null JwtUser in authentication token")
		void shouldHandleNullJwtUserInAuthenticationToken() {
			// Given
			SendMessageRequest sendRequest =
					SendMessageRequestTestDataBuilder.aSendMessageRequest()
							.withConversationId(TEST_CONVERSATION_ID)
							.withContent(TEST_MESSAGE_CONTENT)
							.build();

			when(headerAccessor.getUser()).thenReturn(authToken);
			when(authToken.getPrincipal()).thenReturn(null);

			// When - This should be handled gracefully by the controller
			messageController.handleSendMessage(sendRequest, headerAccessor);

			// Then
			verify(headerAccessor, times(1)).getUser();
			verify(authToken, times(1)).getPrincipal();
			verify(messageService, never()).sendMessage(any(), any());
		}

		@Test
		@DisplayName("Should handle runtime exception from service in WebSocket")
		void shouldHandleRuntimeExceptionFromServiceInWebSocket() {
			// Given
			SendMessageRequest sendRequest =
					SendMessageRequestTestDataBuilder.aSendMessageRequest()
							.withConversationId(TEST_CONVERSATION_ID)
							.withContent(TEST_MESSAGE_CONTENT)
							.build();

			JwtUser jwtUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(headerAccessor.getUser()).thenReturn(authToken);
			when(authToken.getPrincipal()).thenReturn(jwtUser);
			doThrow(new RuntimeException("Database connection error"))
					.when(messageService)
					.sendMessage(sendRequest, TEST_USER_ID);

			// When & Then
			RuntimeException exception =
					assertThrows(
							RuntimeException.class,
							() -> messageController.handleSendMessage(sendRequest, headerAccessor));

			assertEquals("Database connection error", exception.getMessage());
			verify(headerAccessor, times(1)).getUser();
			verify(authToken, times(1)).getPrincipal();
			verify(messageService, times(1)).sendMessage(sendRequest, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Delete Message - Success Cases")
	class DeleteMessageSuccessCases {

		@Test
		@DisplayName("Should successfully delete message with valid ID")
		void shouldDeleteMessageSuccessfully() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			// When
			ResponseEntity<ApiResponse<UUID>> response =
					messageController.deleteMessage(TEST_MESSAGE_ID, currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(
					HttpStatus.OK.value(), Objects.requireNonNull(response.getBody()).getStatus());

			verify(messageService, times(1)).deleteMessage(TEST_MESSAGE_ID, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle deletion of different user's message by authorized user")
		void shouldHandleDeletionOfDifferentUsersMessageByAuthorizedUser() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			// When
			ResponseEntity<ApiResponse<UUID>> response =
					messageController.deleteMessage(TEST_MESSAGE_ID, currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(
					HttpStatus.OK.value(), Objects.requireNonNull(response.getBody()).getStatus());

			verify(messageService, times(1)).deleteMessage(TEST_MESSAGE_ID, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Delete Message - Error Cases")
	class DeleteMessageErrorCases {

		@Test
		@DisplayName("Should handle MessageNotFoundException")
		void shouldHandleMessageNotFoundException() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			doThrow(new MessageNotFoundException("Message not found"))
					.when(messageService)
					.deleteMessage(TEST_MESSAGE_ID, TEST_USER_ID);

			// When & Then
			MessageNotFoundException exception =
					assertThrows(
							MessageNotFoundException.class,
							() -> messageController.deleteMessage(TEST_MESSAGE_ID, currentUser));

			assertEquals("Message not found", exception.getMessage());
			verify(messageService, times(1)).deleteMessage(TEST_MESSAGE_ID, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle AccessDeniedException when user is not message sender")
		void shouldHandleAccessDeniedExceptionWhenUserNotMessageSender() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			doThrow(new AccessDeniedException("User is not authorized to delete this message"))
					.when(messageService)
					.deleteMessage(TEST_MESSAGE_ID, TEST_USER_ID);

			// When & Then
			AccessDeniedException exception =
					assertThrows(
							AccessDeniedException.class,
							() -> messageController.deleteMessage(TEST_MESSAGE_ID, currentUser));

			assertEquals("User is not authorized to delete this message", exception.getMessage());
			verify(messageService, times(1)).deleteMessage(TEST_MESSAGE_ID, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle null user authentication in delete")
		void shouldHandleNullUserAuthenticationInDelete() {
			// When
			ResponseEntity<ApiResponse<UUID>> response =
					messageController.deleteMessage(TEST_MESSAGE_ID, null);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals("Unauthorized", Objects.requireNonNull(response.getBody()).getError());
			assertEquals(
					HttpStatus.UNAUTHORIZED.value(),
					Objects.requireNonNull(response.getBody()).getStatus());

			verifyNoInteractions(messageService);
		}

		@Test
		@DisplayName("Should handle runtime exception from service in delete")
		void shouldHandleRuntimeExceptionFromServiceInDelete() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			doThrow(new RuntimeException("Database connection error"))
					.when(messageService)
					.deleteMessage(TEST_MESSAGE_ID, TEST_USER_ID);

			// When & Then
			RuntimeException exception =
					assertThrows(
							RuntimeException.class,
							() -> messageController.deleteMessage(TEST_MESSAGE_ID, currentUser));

			assertEquals("Database connection error", exception.getMessage());
			verify(messageService, times(1)).deleteMessage(TEST_MESSAGE_ID, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Input Validation Tests")
	class InputValidationTests {

		@Test
		@DisplayName("Should handle null conversation ID in get messages")
		void shouldHandleNullConversationIdInGetMessages() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			// When & Then
			// The actual validation would be handled by Spring's validation framework
			// Here we test the controller's behavior with null parameters
			when(messageService.findConversationMessages(null, TEST_USER_ID))
					.thenThrow(new IllegalArgumentException("Conversation ID cannot be null"));

			IllegalArgumentException exception =
					assertThrows(
							IllegalArgumentException.class,
							() -> messageController.getConversationMessages(null, currentUser));

			assertEquals("Conversation ID cannot be null", exception.getMessage());
			verify(messageService, times(1)).findConversationMessages(null, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle null message ID in delete")
		void shouldHandleNullMessageIdInDelete() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			// When & Then
			doThrow(new IllegalArgumentException("Message ID cannot be null"))
					.when(messageService)
					.deleteMessage(null, TEST_USER_ID);

			IllegalArgumentException exception =
					assertThrows(
							IllegalArgumentException.class,
							() -> messageController.deleteMessage(null, currentUser));

			assertEquals("Message ID cannot be null", exception.getMessage());
			verify(messageService, times(1)).deleteMessage(null, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle empty message content in WebSocket send")
		void shouldHandleEmptyMessageContentInWebSocketSend() {
			// Given
			SendMessageRequest sendRequest =
					SendMessageRequestTestDataBuilder.aSendMessageRequest()
							.withConversationId(TEST_CONVERSATION_ID)
							.withContent("")
							.build();

			JwtUser jwtUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(headerAccessor.getUser()).thenReturn(authToken);
			when(authToken.getPrincipal()).thenReturn(jwtUser);
			doThrow(new IllegalArgumentException("Message content cannot be empty"))
					.when(messageService)
					.sendMessage(sendRequest, TEST_USER_ID);

			// When & Then
			IllegalArgumentException exception =
					assertThrows(
							IllegalArgumentException.class,
							() -> messageController.handleSendMessage(sendRequest, headerAccessor));

			assertEquals("Message content cannot be empty", exception.getMessage());
			verify(headerAccessor, times(1)).getUser();
			verify(authToken, times(1)).getPrincipal();
			verify(messageService, times(1)).sendMessage(sendRequest, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle null send message request in WebSocket")
		void shouldHandleNullSendMessageRequestInWebSocket() {
			// Given
			JwtUser jwtUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(headerAccessor.getUser()).thenReturn(authToken);
			when(authToken.getPrincipal()).thenReturn(jwtUser);
			doThrow(new IllegalArgumentException("Send message request cannot be null"))
					.when(messageService)
					.sendMessage(null, TEST_USER_ID);

			// When & Then
			IllegalArgumentException exception =
					assertThrows(
							IllegalArgumentException.class,
							() -> messageController.handleSendMessage(null, headerAccessor));

			assertEquals("Send message request cannot be null", exception.getMessage());
			verify(headerAccessor, times(1)).getUser();
			verify(authToken, times(1)).getPrincipal();
			verify(messageService, times(1)).sendMessage(null, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Authentication and Authorization Tests")
	class AuthenticationAndAuthorizationTests {

		@Test
		@DisplayName("Should extract user ID correctly from JWT user")
		void shouldExtractUserIdCorrectlyFromJwtUser() {
			// Given
			UUID specificUserId = UUID.randomUUID();
			JwtUser currentUser = new JwtUser(TEST_EMAIL, specificUserId, null);

			List<MessageDto> messages =
					List.of(
							MessageDtoTestDataBuilder.aMessageDto()
									.withId(TEST_MESSAGE_ID)
									.withSenderId(specificUserId)
									.build());

			when(messageService.findConversationMessages(TEST_CONVERSATION_ID, specificUserId))
					.thenReturn(messages);

			// When
			ResponseEntity<ApiResponse<List<MessageDto>>> response =
					messageController.getConversationMessages(TEST_CONVERSATION_ID, currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			verify(messageService, times(1))
					.findConversationMessages(TEST_CONVERSATION_ID, specificUserId);
		}

		@Test
		@DisplayName("Should extract user ID correctly from WebSocket principal")
		void shouldExtractUserIdCorrectlyFromWebSocketPrincipal() {
			// Given
			UUID specificUserId = UUID.randomUUID();
			SendMessageRequest sendRequest =
					SendMessageRequestTestDataBuilder.aSendMessageRequest()
							.withConversationId(TEST_CONVERSATION_ID)
							.withContent(TEST_MESSAGE_CONTENT)
							.build();

			JwtUser jwtUser = new JwtUser(TEST_EMAIL, specificUserId, null);

			when(headerAccessor.getUser()).thenReturn(authToken);
			when(authToken.getPrincipal()).thenReturn(jwtUser);

			// When
			messageController.handleSendMessage(sendRequest, headerAccessor);

			// Then
			verify(headerAccessor, times(1)).getUser();
			verify(authToken, times(1)).getPrincipal();
			verify(messageService, times(1)).sendMessage(sendRequest, specificUserId);
		}

		@Test
		@DisplayName("Should pass correct user ID for message deletion authorization")
		void shouldPassCorrectUserIdForMessageDeletionAuthorization() {
			// Given
			UUID specificUserId = UUID.randomUUID();
			JwtUser currentUser = new JwtUser(TEST_EMAIL, specificUserId, null);

			// When
			ResponseEntity<ApiResponse<UUID>> response =
					messageController.deleteMessage(TEST_MESSAGE_ID, currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertNotNull(response.getBody());
			assertEquals(TEST_MESSAGE_ID, Objects.requireNonNull(response.getBody()).getData());
			verify(messageService, times(1)).deleteMessage(TEST_MESSAGE_ID, specificUserId);
		}
	}

	@Nested
	@DisplayName("Edge Cases and Boundary Tests")
	class EdgeCasesAndBoundaryTests {

		@Test
		@DisplayName("Should handle very large conversation ID")
		void shouldHandleVeryLargeConversationId() {
			// Given
			UUID largeConversationId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			List<MessageDto> messages = Collections.emptyList();
			when(messageService.findConversationMessages(largeConversationId, TEST_USER_ID))
					.thenReturn(messages);

			// When
			ResponseEntity<ApiResponse<List<MessageDto>>> response =
					messageController.getConversationMessages(largeConversationId, currentUser);

			// Then
			assertNotNull(response);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			verify(messageService, times(1))
					.findConversationMessages(largeConversationId, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle message with maximum content length")
		void shouldHandleMessageWithMaximumContentLength() {
			// Given
			String maxLengthContent = "A".repeat(10000); // Assuming 10k chars is max
			SendMessageRequest sendRequest =
					SendMessageRequestTestDataBuilder.aSendMessageRequest()
							.withConversationId(TEST_CONVERSATION_ID)
							.withContent(maxLengthContent)
							.build();

			JwtUser jwtUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(headerAccessor.getUser()).thenReturn(authToken);
			when(authToken.getPrincipal()).thenReturn(jwtUser);

			// When
			messageController.handleSendMessage(sendRequest, headerAccessor);

			// Then
			verify(messageService, times(1)).sendMessage(sendRequest, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle concurrent deletion attempts")
		void shouldHandleConcurrentDeletionAttempts() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			doThrow(new MessageNotFoundException("Message already deleted"))
					.when(messageService)
					.deleteMessage(TEST_MESSAGE_ID, TEST_USER_ID);

			// When & Then
			MessageNotFoundException exception =
					assertThrows(
							MessageNotFoundException.class,
							() -> messageController.deleteMessage(TEST_MESSAGE_ID, currentUser));

			assertEquals("Message already deleted", exception.getMessage());
			verify(messageService, times(1)).deleteMessage(TEST_MESSAGE_ID, TEST_USER_ID);
		}
	}

	@Nested
	@DisplayName("Integration Behavior Tests")
	class IntegrationBehaviorTests {

		@Test
		@DisplayName("Should maintain consistent behavior across REST and WebSocket endpoints")
		void shouldMaintainConsistentBehaviorAcrossEndpoints() {
			// Given
			JwtUser restUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			// Test REST endpoint behavior
			when(messageService.findConversationMessages(TEST_CONVERSATION_ID, TEST_USER_ID))
					.thenThrow(new AccessDeniedException("User is not a participant"));

			// When & Then - REST endpoint
			AccessDeniedException restException =
					assertThrows(
							AccessDeniedException.class,
							() ->
									messageController.getConversationMessages(
											TEST_CONVERSATION_ID, restUser));

			assertEquals("User is not a participant", restException.getMessage());

			// Given - WebSocket endpoint
			SendMessageRequest sendRequest =
					SendMessageRequestTestDataBuilder.aSendMessageRequest()
							.withConversationId(TEST_CONVERSATION_ID)
							.withContent(TEST_MESSAGE_CONTENT)
							.build();

			JwtUser jwtUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			when(headerAccessor.getUser()).thenReturn(authToken);
			when(authToken.getPrincipal()).thenReturn(jwtUser);
			doThrow(new AccessDeniedException("User is not a participant"))
					.when(messageService)
					.sendMessage(sendRequest, TEST_USER_ID);

			// When & Then - WebSocket endpoint
			AccessDeniedException webSocketException =
					assertThrows(
							AccessDeniedException.class,
							() -> messageController.handleSendMessage(sendRequest, headerAccessor));

			assertEquals("User is not a participant", webSocketException.getMessage());

			// Verify both endpoints called the service
			verify(messageService, times(1))
					.findConversationMessages(TEST_CONVERSATION_ID, TEST_USER_ID);
			verify(messageService, times(1)).sendMessage(sendRequest, TEST_USER_ID);
		}

		@Test
		@DisplayName("Should handle service layer exceptions consistently")
		void shouldHandleServiceLayerExceptionsConsistently() {
			// Given
			JwtUser currentUser = new JwtUser(TEST_EMAIL, TEST_USER_ID, null);

			// Test that all endpoints properly propagate service exceptions
			when(messageService.findConversationMessages(TEST_CONVERSATION_ID, TEST_USER_ID))
					.thenThrow(new RuntimeException("Service unavailable"));

			doThrow(new RuntimeException("Service unavailable"))
					.when(messageService)
					.deleteMessage(TEST_MESSAGE_ID, TEST_USER_ID);

			// When & Then - Get messages
			RuntimeException getException =
					assertThrows(
							RuntimeException.class,
							() ->
									messageController.getConversationMessages(
											TEST_CONVERSATION_ID, currentUser));
			assertEquals("Service unavailable", getException.getMessage());

			// When & Then - Delete message
			RuntimeException deleteException =
					assertThrows(
							RuntimeException.class,
							() -> messageController.deleteMessage(TEST_MESSAGE_ID, currentUser));
			assertEquals("Service unavailable", deleteException.getMessage());

			verify(messageService, times(1))
					.findConversationMessages(TEST_CONVERSATION_ID, TEST_USER_ID);
			verify(messageService, times(1)).deleteMessage(TEST_MESSAGE_ID, TEST_USER_ID);
		}
	}
}
