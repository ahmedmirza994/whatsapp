/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.websocket;

import static com.ah.whatsapp.constant.WebSocketConstants.CONVERSATION_QUEUE;
import static com.ah.whatsapp.constant.WebSocketConstants.CONVERSATION_TOPIC_TEMPLATE;
import static com.ah.whatsapp.constant.WebSocketConstants.TYPING_INDICATOR_TOPIC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.ah.whatsapp.dto.ConversationDto;
import com.ah.whatsapp.dto.ConversationDtoTestDataBuilder;
import com.ah.whatsapp.dto.DeleteMessageEvent;
import com.ah.whatsapp.dto.MessageDto;
import com.ah.whatsapp.dto.MessageDtoTestDataBuilder;
import com.ah.whatsapp.dto.ParticipantDto;
import com.ah.whatsapp.dto.ParticipantDtoTestDataBuilder;
import com.ah.whatsapp.dto.TypingIndicatorDto;
import com.ah.whatsapp.dto.TypingIndicatorDtoTestDataBuilder;
import com.ah.whatsapp.dto.WebSocketEvent;
import com.ah.whatsapp.enums.EventType;
import com.ah.whatsapp.event.ConversationUpdateEvent;
import com.ah.whatsapp.event.MessageDeletedEvent;
import com.ah.whatsapp.event.NewMessageEvent;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketEventListener Tests")
class WebSocketEventListenerTest {

	@Mock private SimpMessagingTemplate messagingTemplate;

	@InjectMocks private WebSocketEventListener webSocketEventListener;

	private UUID conversationId;
	private UUID messageId;
	private UUID userId;

	@BeforeEach
	void setUp() {
		conversationId = UUID.randomUUID();
		messageId = UUID.randomUUID();
		userId = UUID.randomUUID();
	}

	@Test
	@DisplayName("Should handle new message event and send to conversation topic")
	void handleNewMessage_ShouldSendMessageToConversationTopic() {
		// Given
		MessageDto messageDto =
				MessageDtoTestDataBuilder.aMessageDto()
						.withId(messageId)
						.withConversationId(conversationId)
						.withContent("Test message")
						.withSenderId(userId)
						.withSentAt(LocalDateTime.now())
						.build();

		NewMessageEvent event = new NewMessageEvent(this, messageDto);
		String expectedDestination = String.format(CONVERSATION_TOPIC_TEMPLATE, conversationId);

		// When
		webSocketEventListener.handleNewMessage(event);

		// Then
		ArgumentCaptor<WebSocketEvent<?>> eventCaptor =
				ArgumentCaptor.forClass(WebSocketEvent.class);
		verify(messagingTemplate).convertAndSend(eq(expectedDestination), eventCaptor.capture());

		WebSocketEvent<?> capturedEvent = eventCaptor.getValue();
		assert capturedEvent.getType() == EventType.NEW_MESSAGE;
		assert capturedEvent.getPayload().equals(messageDto);
	}

	@Test
	@DisplayName("Should handle conversation update event and send to all participants")
	void handleConversationUpdate_ShouldSendToAllParticipants() {
		// Given
		ParticipantDto participant1 =
				ParticipantDtoTestDataBuilder.aParticipantDto()
						.withId(UUID.randomUUID())
						.withEmail("user1@example.com")
						.withName("User 1")
						.build();

		ParticipantDto participant2 =
				ParticipantDtoTestDataBuilder.aParticipantDto()
						.withId(UUID.randomUUID())
						.withEmail("user2@example.com")
						.withName("User 2")
						.build();

		ConversationDto conversationDto =
				ConversationDtoTestDataBuilder.aConversationDto()
						.withId(conversationId)
						.withParticipants(List.of(participant1, participant2))
						.build();

		ConversationUpdateEvent event = new ConversationUpdateEvent(this, conversationDto);

		// When
		webSocketEventListener.handleConversationUpdate(event);

		// Then
		ArgumentCaptor<WebSocketEvent<?>> eventCaptor =
				ArgumentCaptor.forClass(WebSocketEvent.class);
		verify(messagingTemplate, times(2))
				.convertAndSendToUser(
						any(String.class), eq(CONVERSATION_QUEUE), eventCaptor.capture());

		// Verify first participant
		verify(messagingTemplate)
				.convertAndSendToUser(
						eq("user1@example.com"), eq(CONVERSATION_QUEUE), any(WebSocketEvent.class));

		// Verify second participant
		verify(messagingTemplate)
				.convertAndSendToUser(
						eq("user2@example.com"), eq(CONVERSATION_QUEUE), any(WebSocketEvent.class));

		// Verify event content
		List<WebSocketEvent<?>> capturedEvents = eventCaptor.getAllValues();
		capturedEvents.forEach(
				capturedEvent -> {
					assert capturedEvent.getType() == EventType.CONVERSATION_UPDATE;
					assert capturedEvent.getPayload().equals(conversationDto);
				});
	}

	@Test
	@DisplayName("Should handle conversation update with participant without email")
	void handleConversationUpdate_WithParticipantWithoutEmail_ShouldSkipParticipant() {
		// Given
		ParticipantDto participantWithEmail =
				ParticipantDtoTestDataBuilder.aParticipantDto()
						.withId(UUID.randomUUID())
						.withEmail("user1@example.com")
						.withName("User 1")
						.build();

		ParticipantDto participantWithoutEmail =
				ParticipantDtoTestDataBuilder.aParticipantDto()
						.withId(UUID.randomUUID())
						.withEmail(null)
						.withName("User 2")
						.build();

		ConversationDto conversationDto =
				ConversationDtoTestDataBuilder.aConversationDto()
						.withId(conversationId)
						.withParticipants(List.of(participantWithEmail, participantWithoutEmail))
						.build();

		ConversationUpdateEvent event = new ConversationUpdateEvent(this, conversationDto);

		// When
		webSocketEventListener.handleConversationUpdate(event);

		// Then
		// Should only send to participant with email
		verify(messagingTemplate, times(1))
				.convertAndSendToUser(
						eq("user1@example.com"), eq(CONVERSATION_QUEUE), any(WebSocketEvent.class));
	}

	@Test
	@DisplayName("Should handle conversation update with empty participants list")
	void handleConversationUpdate_WithEmptyParticipants_ShouldNotSendAnyMessages() {
		// Given
		ConversationDto conversationDto =
				ConversationDtoTestDataBuilder.aConversationDto()
						.withId(conversationId)
						.withParticipants(List.of())
						.build();

		ConversationUpdateEvent event = new ConversationUpdateEvent(this, conversationDto);

		// When
		webSocketEventListener.handleConversationUpdate(event);

		// Then
		verifyNoInteractions(messagingTemplate);
	}

	@Test
	@DisplayName("Should handle message deleted event and send to conversation topic")
	void handleMessageDeleted_ShouldSendDeleteEventToConversationTopic() {
		// Given
		MessageDeletedEvent event = new MessageDeletedEvent(this, messageId, conversationId);
		String expectedDestination = String.format(CONVERSATION_TOPIC_TEMPLATE, conversationId);

		// When
		webSocketEventListener.handleMessageDeleted(event);

		// Then
		ArgumentCaptor<WebSocketEvent<?>> eventCaptor =
				ArgumentCaptor.forClass(WebSocketEvent.class);
		verify(messagingTemplate).convertAndSend(eq(expectedDestination), eventCaptor.capture());

		WebSocketEvent<?> capturedEvent = eventCaptor.getValue();
		assert capturedEvent.getType() == EventType.DELETE_MESSAGE;

		DeleteMessageEvent deleteMessageEvent = (DeleteMessageEvent) capturedEvent.getPayload();
		assert deleteMessageEvent.messageId().equals(messageId);
		assert deleteMessageEvent.conversationId().equals(conversationId);
	}

	@Test
	@DisplayName("Should handle typing indicator event and send to typing topic")
	void handleTypingIndicator_ShouldSendToTypingTopic() {
		// Given
		TypingIndicatorDto typingDto =
				TypingIndicatorDtoTestDataBuilder.aTypingIndicatorDto()
						.withConversationId(conversationId)
						.withUserId(userId)
						.withEventType(EventType.TYPING_START)
						.build();

		String expectedDestination = String.format(TYPING_INDICATOR_TOPIC, conversationId);

		// When
		webSocketEventListener.handleTypingIndicator(typingDto);

		// Then
		ArgumentCaptor<WebSocketEvent<?>> eventCaptor =
				ArgumentCaptor.forClass(WebSocketEvent.class);
		verify(messagingTemplate).convertAndSend(eq(expectedDestination), eventCaptor.capture());

		WebSocketEvent<?> capturedEvent = eventCaptor.getValue();
		assert capturedEvent.getType() == EventType.TYPING_START;
		assert capturedEvent.getPayload().equals(typingDto);
	}

	@Test
	@DisplayName("Should handle typing stop indicator event")
	void handleTypingIndicator_WithTypingStop_ShouldSendCorrectEvent() {
		// Given
		TypingIndicatorDto typingDto =
				TypingIndicatorDtoTestDataBuilder.aTypingIndicatorDto()
						.withConversationId(conversationId)
						.withUserId(userId)
						.withEventType(EventType.TYPING_STOP)
						.build();

		String expectedDestination = String.format(TYPING_INDICATOR_TOPIC, conversationId);

		// When
		webSocketEventListener.handleTypingIndicator(typingDto);

		// Then
		ArgumentCaptor<WebSocketEvent<?>> eventCaptor =
				ArgumentCaptor.forClass(WebSocketEvent.class);
		verify(messagingTemplate).convertAndSend(eq(expectedDestination), eventCaptor.capture());

		WebSocketEvent<?> capturedEvent = eventCaptor.getValue();
		assert capturedEvent.getType() == EventType.TYPING_STOP;
		assert capturedEvent.getPayload().equals(typingDto);
	}

	@Test
	@DisplayName("Should format conversation topic destination correctly")
	void handleNewMessage_ShouldFormatDestinationCorrectly() {
		// Given
		MessageDto messageDto =
				MessageDtoTestDataBuilder.aMessageDto()
						.withId(messageId)
						.withConversationId(conversationId)
						.withContent("Test message")
						.withSenderId(userId)
						.build();

		NewMessageEvent event = new NewMessageEvent(this, messageDto);

		// When
		webSocketEventListener.handleNewMessage(event);

		// Then
		String expectedDestination = "/topic/conversations/" + conversationId;
		verify(messagingTemplate)
				.convertAndSend(eq(expectedDestination), any(WebSocketEvent.class));
	}

	@Test
	@DisplayName("Should format typing indicator destination correctly")
	void handleTypingIndicator_ShouldFormatDestinationCorrectly() {
		// Given
		TypingIndicatorDto typingDto =
				TypingIndicatorDtoTestDataBuilder.aTypingIndicatorDto()
						.withConversationId(conversationId)
						.withUserId(userId)
						.withEventType(EventType.TYPING_START)
						.build();

		// When
		webSocketEventListener.handleTypingIndicator(typingDto);

		// Then
		String expectedDestination = "/topic/conversations/" + conversationId + "/typing";
		verify(messagingTemplate)
				.convertAndSend(eq(expectedDestination), any(WebSocketEvent.class));
	}
}
