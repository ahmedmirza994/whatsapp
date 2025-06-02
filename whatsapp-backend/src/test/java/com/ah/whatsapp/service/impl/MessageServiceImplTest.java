/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.service.impl;

import static com.ah.whatsapp.dto.MessageDtoTestDataBuilder.aMessageDto;
import static com.ah.whatsapp.dto.SendMessageRequestTestDataBuilder.aSendMessageRequest;
import static com.ah.whatsapp.mapper.ConversationParticipantTestDataBuilder.aConversationParticipant;
import static com.ah.whatsapp.mapper.ConversationTestDataBuilder.aConversation;
import static com.ah.whatsapp.mapper.MessageTestDataBuilder.aMessage;
import static com.ah.whatsapp.mapper.UserTestDataBuilder.aUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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
import com.ah.whatsapp.dto.MessageDto;
import com.ah.whatsapp.dto.SendMessageRequest;
import com.ah.whatsapp.event.ConversationUpdateEvent;
import com.ah.whatsapp.event.MessageDeletedEvent;
import com.ah.whatsapp.event.NewMessageEvent;
import com.ah.whatsapp.exception.ConversationNotFoundException;
import com.ah.whatsapp.exception.MessageNotFoundException;
import com.ah.whatsapp.exception.UserNotFoundException;
import com.ah.whatsapp.mapper.ConversationMapper;
import com.ah.whatsapp.mapper.MessageMapper;
import com.ah.whatsapp.model.Conversation;
import com.ah.whatsapp.model.ConversationParticipant;
import com.ah.whatsapp.model.Message;
import com.ah.whatsapp.model.User;
import com.ah.whatsapp.repository.ConversationParticipantRepository;
import com.ah.whatsapp.repository.ConversationRepository;
import com.ah.whatsapp.repository.MessageRepository;
import com.ah.whatsapp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService Tests")
class MessageServiceImplTest {

	@Mock private MessageRepository messageRepository;

	@Mock private ConversationRepository conversationRepository;

	@Mock private UserRepository userRepository;

	@Mock private ConversationParticipantRepository conversationParticipantRepository;

	@Mock private MessageMapper messageMapper;

	@Mock private ConversationMapper conversationMapper;

	@Mock private ApplicationEventPublisher eventPublisher;

	@InjectMocks private MessageServiceImpl messageService;

	private UUID senderId;
	private UUID conversationId;
	private UUID messageId;
	private User sender;
	private Conversation conversation;
	private Message message;
	private SendMessageRequest sendMessageRequest;
	private MessageDto messageDto;
	private ConversationParticipant participant;

	@BeforeEach
	void setUp() {
		senderId = UUID.randomUUID();
		conversationId = UUID.randomUUID();
		messageId = UUID.randomUUID();

		sender = aUser().withId(senderId).build();
		conversation = aConversation().withId(conversationId).build();
		message =
				aMessage()
						.withId(messageId)
						.withConversationId(conversationId)
						.withSender(sender)
						.build();
		sendMessageRequest =
				aSendMessageRequest()
						.withConversationId(conversationId)
						.withContent("Test message")
						.build();
		messageDto = aMessageDto().withId(messageId).build();
		participant =
				aConversationParticipant()
						.withConversationId(conversationId)
						.withParticipantId(senderId)
						.withActive(true)
						.build();
	}

	@Nested
	@DisplayName("Send Message Tests")
	class SendMessageTests {

		@Test
		@DisplayName("Should send message successfully when all conditions are met")
		void shouldSendMessageSuccessfully() {
			// Given
			ConversationParticipant inactiveParticipant =
					aConversationParticipant()
							.withConversationId(conversationId)
							.withParticipantId(UUID.randomUUID())
							.withActive(false)
							.build();

			List<ConversationParticipant> participants =
					Arrays.asList(participant, inactiveParticipant);
			ConversationDto conversationDto = new ConversationDto();
			conversationDto.setId(conversationId);
			conversationDto.setCreatedAt(LocalDateTime.now());
			conversationDto.setUpdatedAt(LocalDateTime.now());

			when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
			when(conversationRepository.findById(conversationId))
					.thenReturn(Optional.of(conversation));
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, senderId))
					.thenReturn(true);
			when(conversationParticipantRepository.findByConversationId(conversationId))
					.thenReturn(participants);
			when(messageRepository.save(any(Message.class))).thenReturn(message);
			when(messageMapper.toDto(message)).thenReturn(messageDto);
			when(conversationMapper.toDto(conversation)).thenReturn(conversationDto);

			// When
			MessageDto result = messageService.sendMessage(sendMessageRequest, senderId);

			// Then
			assertNotNull(result);
			assertEquals(messageId, result.id());

			// Verify repository interactions
			verify(userRepository).findById(senderId);
			verify(conversationRepository, times(2)).findById(conversationId);
			verify(conversationParticipantRepository)
					.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, senderId);
			verify(conversationParticipantRepository).findByConversationId(conversationId);
			verify(messageRepository).save(any(Message.class));
			verify(conversationRepository).save(any(Conversation.class));

			// Verify inactive participant was activated
			verify(conversationParticipantRepository).save(eq(inactiveParticipant));

			// Verify events were published
			verify(eventPublisher).publishEvent(any(NewMessageEvent.class));
			verify(eventPublisher).publishEvent(any(ConversationUpdateEvent.class));

			// Verify message mapping
			verify(messageMapper).toDto(message);
			verify(conversationMapper).toDto(conversation);
		}

		@Test
		@DisplayName("Should throw UserNotFoundException when sender does not exist")
		void shouldThrowUserNotFoundExceptionWhenSenderDoesNotExist() {
			// Given
			when(userRepository.findById(senderId)).thenReturn(Optional.empty());

			// When & Then
			UserNotFoundException exception =
					assertThrows(
							UserNotFoundException.class,
							() -> messageService.sendMessage(sendMessageRequest, senderId));

			assertEquals("User not found", exception.getMessage());

			// Verify no further interactions
			verify(userRepository).findById(senderId);
			verify(conversationRepository, never()).findById(any());
			verify(messageRepository, never()).save(any());
			verify(eventPublisher, never()).publishEvent(any());
		}

		@Test
		@DisplayName("Should throw ConversationNotFoundException when conversation does not exist")
		void shouldThrowConversationNotFoundExceptionWhenConversationDoesNotExist() {
			// Given
			when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
			when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

			// When & Then
			ConversationNotFoundException exception =
					assertThrows(
							ConversationNotFoundException.class,
							() -> messageService.sendMessage(sendMessageRequest, senderId));

			assertEquals("Conversation not found", exception.getMessage());

			// Verify interactions
			verify(userRepository).findById(senderId);
			verify(conversationRepository).findById(conversationId);
			verify(messageRepository, never()).save(any());
			verify(eventPublisher, never()).publishEvent(any());
		}

		@Test
		@DisplayName("Should throw AccessDeniedException when user is not a participant")
		void shouldThrowAccessDeniedExceptionWhenUserIsNotParticipant() {
			// Given
			when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
			when(conversationRepository.findById(conversationId))
					.thenReturn(Optional.of(conversation));
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, senderId))
					.thenReturn(false);

			// When & Then
			AccessDeniedException exception =
					assertThrows(
							AccessDeniedException.class,
							() -> messageService.sendMessage(sendMessageRequest, senderId));

			assertEquals("User is not a participant in this conversation", exception.getMessage());

			// Verify interactions
			verify(userRepository).findById(senderId);
			verify(conversationRepository).findById(conversationId);
			verify(conversationParticipantRepository)
					.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, senderId);
			verify(messageRepository, never()).save(any());
			verify(eventPublisher, never()).publishEvent(any());
		}

		@Test
		@DisplayName("Should activate all inactive participants when sending message")
		void shouldActivateAllInactiveParticipants() {
			// Given
			ConversationParticipant inactiveParticipant1 =
					aConversationParticipant()
							.withConversationId(conversationId)
							.withParticipantId(UUID.randomUUID())
							.withActive(false)
							.build();

			ConversationParticipant inactiveParticipant2 =
					aConversationParticipant()
							.withConversationId(conversationId)
							.withParticipantId(UUID.randomUUID())
							.withActive(false)
							.build();

			List<ConversationParticipant> participants =
					Arrays.asList(participant, inactiveParticipant1, inactiveParticipant2);
			ConversationDto conversationDto = new ConversationDto();
			conversationDto.setId(conversationId);
			conversationDto.setCreatedAt(LocalDateTime.now());
			conversationDto.setUpdatedAt(LocalDateTime.now());

			when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
			when(conversationRepository.findById(conversationId))
					.thenReturn(Optional.of(conversation));
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, senderId))
					.thenReturn(true);
			when(conversationParticipantRepository.findByConversationId(conversationId))
					.thenReturn(participants);
			when(messageRepository.save(any(Message.class))).thenReturn(message);
			when(messageMapper.toDto(message)).thenReturn(messageDto);
			when(conversationMapper.toDto(conversation)).thenReturn(conversationDto);

			// When
			messageService.sendMessage(sendMessageRequest, senderId);

			// Then
			verify(conversationParticipantRepository).save(eq(inactiveParticipant1));
			verify(conversationParticipantRepository).save(eq(inactiveParticipant2));
			verify(conversationParticipantRepository, never())
					.save(eq(participant)); // Already active
		}
	}

	@Nested
	@DisplayName("Find Conversation Messages Tests")
	class FindConversationMessagesTests {

		@Test
		@DisplayName("Should return conversation messages successfully when user is participant")
		void shouldReturnConversationMessagesSuccessfully() {
			// Given
			LocalDateTime joinedAt = LocalDateTime.now().minusDays(1);
			participant =
					aConversationParticipant()
							.withConversationId(conversationId)
							.withParticipantId(senderId)
							.withActive(true)
							.withJoinedAt(joinedAt)
							.build();

			Message message1 =
					aMessage().withId(UUID.randomUUID()).withConversationId(conversationId).build();
			Message message2 =
					aMessage().withId(UUID.randomUUID()).withConversationId(conversationId).build();
			List<Message> messages = Arrays.asList(message1, message2);

			MessageDto messageDto1 = aMessageDto().withId(message1.getId()).build();
			MessageDto messageDto2 = aMessageDto().withId(message2.getId()).build();

			when(conversationRepository.existsById(conversationId)).thenReturn(true);
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, senderId))
					.thenReturn(true);
			when(conversationParticipantRepository.findByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, senderId))
					.thenReturn(Optional.of(participant));
			when(messageRepository.findByConversationIdAndSentAtAfter(conversationId, joinedAt))
					.thenReturn(messages);
			when(messageMapper.toDto(message1)).thenReturn(messageDto1);
			when(messageMapper.toDto(message2)).thenReturn(messageDto2);

			// When
			List<MessageDto> result =
					messageService.findConversationMessages(conversationId, senderId);

			// Then
			assertNotNull(result);
			assertEquals(2, result.size());
			assertEquals(messageDto1.id(), result.get(0).id());
			assertEquals(messageDto2.id(), result.get(1).id());

			// Verify interactions
			verify(conversationRepository).existsById(conversationId);
			verify(conversationParticipantRepository)
					.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, senderId);
			verify(conversationParticipantRepository)
					.findByConversationIdAndUserIdAndIsActiveTrue(conversationId, senderId);
			verify(messageRepository).findByConversationIdAndSentAtAfter(conversationId, joinedAt);
			verify(messageMapper).toDto(message1);
			verify(messageMapper).toDto(message2);
		}

		@Test
		@DisplayName("Should throw ConversationNotFoundException when conversation does not exist")
		void shouldThrowConversationNotFoundExceptionWhenConversationDoesNotExist() {
			// Given
			when(conversationRepository.existsById(conversationId)).thenReturn(false);

			// When & Then
			ConversationNotFoundException exception =
					assertThrows(
							ConversationNotFoundException.class,
							() ->
									messageService.findConversationMessages(
											conversationId, senderId));

			assertEquals("Conversation not found", exception.getMessage());

			// Verify interactions
			verify(conversationRepository).existsById(conversationId);
			verify(conversationParticipantRepository, never())
					.existsByConversationIdAndUserIdAndIsActiveTrue(any(), any());
			verify(messageRepository, never()).findByConversationIdAndSentAtAfter(any(), any());
		}

		@Test
		@DisplayName("Should throw AccessDeniedException when user is not an active participant")
		void shouldThrowAccessDeniedExceptionWhenUserIsNotActiveParticipant() {
			// Given
			when(conversationRepository.existsById(conversationId)).thenReturn(true);
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, senderId))
					.thenReturn(false);

			// When & Then
			AccessDeniedException exception =
					assertThrows(
							AccessDeniedException.class,
							() ->
									messageService.findConversationMessages(
											conversationId, senderId));

			assertEquals("User is not a participant in this conversation", exception.getMessage());

			// Verify interactions
			verify(conversationRepository).existsById(conversationId);
			verify(conversationParticipantRepository)
					.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, senderId);
			verify(messageRepository, never()).findByConversationIdAndSentAtAfter(any(), any());
		}

		@Test
		@DisplayName("Should throw AccessDeniedException when participant not found")
		void shouldThrowAccessDeniedExceptionWhenParticipantNotFound() {
			// Given
			when(conversationRepository.existsById(conversationId)).thenReturn(true);
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, senderId))
					.thenReturn(true);
			when(conversationParticipantRepository.findByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, senderId))
					.thenReturn(Optional.empty());

			// When & Then
			AccessDeniedException exception =
					assertThrows(
							AccessDeniedException.class,
							() ->
									messageService.findConversationMessages(
											conversationId, senderId));

			assertEquals("User is not a participant in this conversation", exception.getMessage());

			// Verify interactions
			verify(conversationRepository).existsById(conversationId);
			verify(conversationParticipantRepository)
					.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, senderId);
			verify(conversationParticipantRepository)
					.findByConversationIdAndUserIdAndIsActiveTrue(conversationId, senderId);
			verify(messageRepository, never()).findByConversationIdAndSentAtAfter(any(), any());
		}

		@Test
		@DisplayName("Should return empty list when no messages exist after join date")
		void shouldReturnEmptyListWhenNoMessagesExistAfterJoinDate() {
			// Given
			LocalDateTime joinedAt = LocalDateTime.now().minusDays(1);
			participant =
					aConversationParticipant()
							.withConversationId(conversationId)
							.withParticipantId(senderId)
							.withActive(true)
							.withJoinedAt(joinedAt)
							.build();

			when(conversationRepository.existsById(conversationId)).thenReturn(true);
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, senderId))
					.thenReturn(true);
			when(conversationParticipantRepository.findByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, senderId))
					.thenReturn(Optional.of(participant));
			when(messageRepository.findByConversationIdAndSentAtAfter(conversationId, joinedAt))
					.thenReturn(Collections.emptyList());

			// When
			List<MessageDto> result =
					messageService.findConversationMessages(conversationId, senderId);

			// Then
			assertNotNull(result);
			assertEquals(0, result.size());

			// Verify interactions
			verify(conversationRepository).existsById(conversationId);
			verify(conversationParticipantRepository)
					.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, senderId);
			verify(conversationParticipantRepository)
					.findByConversationIdAndUserIdAndIsActiveTrue(conversationId, senderId);
			verify(messageRepository).findByConversationIdAndSentAtAfter(conversationId, joinedAt);
		}
	}

	@Nested
	@DisplayName("Delete Message Tests")
	class DeleteMessageTests {

		@Test
		@DisplayName("Should delete message successfully when user is the sender")
		void shouldDeleteMessageSuccessfullyWhenUserIsSender() {
			// Given
			when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

			// When
			messageService.deleteMessage(messageId, senderId);

			// Then
			verify(messageRepository).findById(messageId);
			verify(messageRepository).delete(messageId);

			// Verify event publishing
			ArgumentCaptor<MessageDeletedEvent> eventCaptor =
					ArgumentCaptor.forClass(MessageDeletedEvent.class);
			verify(eventPublisher).publishEvent(eventCaptor.capture());

			MessageDeletedEvent capturedEvent = eventCaptor.getValue();
			assertEquals(messageId, capturedEvent.getMessageId());
			assertEquals(conversationId, capturedEvent.getConversationId());
		}

		@Test
		@DisplayName("Should throw MessageNotFoundException when message does not exist")
		void shouldThrowMessageNotFoundExceptionWhenMessageDoesNotExist() {
			// Given
			when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

			// When & Then
			MessageNotFoundException exception =
					assertThrows(
							MessageNotFoundException.class,
							() -> messageService.deleteMessage(messageId, senderId));

			assertEquals("Message not found", exception.getMessage());

			// Verify interactions
			verify(messageRepository).findById(messageId);
			verify(messageRepository, never()).delete(any());
			verify(eventPublisher, never()).publishEvent(any());
		}

		@Test
		@DisplayName("Should throw AccessDeniedException when user is not the sender")
		void shouldThrowAccessDeniedExceptionWhenUserIsNotSender() {
			// Given
			UUID differentUserId = UUID.randomUUID();
			User differentUser = aUser().withId(differentUserId).build();
			Message messageFromDifferentUser =
					aMessage()
							.withId(messageId)
							.withConversationId(conversationId)
							.withSender(differentUser)
							.build();

			when(messageRepository.findById(messageId))
					.thenReturn(Optional.of(messageFromDifferentUser));

			// When & Then
			AccessDeniedException exception =
					assertThrows(
							AccessDeniedException.class,
							() -> messageService.deleteMessage(messageId, senderId));

			assertEquals("You can only delete your own messages.", exception.getMessage());

			// Verify interactions
			verify(messageRepository).findById(messageId);
			verify(messageRepository, never()).delete(any());
			verify(eventPublisher, never()).publishEvent(any());
		}

		@Test
		@DisplayName("Should handle successful deletion with correct event data")
		void shouldHandleSuccessfulDeletionWithCorrectEventData() {
			// Given
			UUID specificConversationId = UUID.randomUUID();
			Message specificMessage =
					aMessage()
							.withId(messageId)
							.withConversationId(specificConversationId)
							.withSender(sender)
							.build();

			when(messageRepository.findById(messageId)).thenReturn(Optional.of(specificMessage));

			// When
			messageService.deleteMessage(messageId, senderId);

			// Then
			verify(messageRepository).findById(messageId);
			verify(messageRepository).delete(messageId);

			// Verify event publishing with correct data
			ArgumentCaptor<MessageDeletedEvent> eventCaptor =
					ArgumentCaptor.forClass(MessageDeletedEvent.class);
			verify(eventPublisher).publishEvent(eventCaptor.capture());

			MessageDeletedEvent capturedEvent = eventCaptor.getValue();
			assertEquals(messageId, capturedEvent.getMessageId());
			assertEquals(specificConversationId, capturedEvent.getConversationId());
			assertEquals(messageService, capturedEvent.getSource());
		}
	}

	@Nested
	@DisplayName("Integration Tests")
	class IntegrationTests {

		@Test
		@DisplayName("Should handle complete message lifecycle - send, find, delete")
		void shouldHandleCompleteMessageLifecycle() {
			// Given - Setup for send message
			ConversationDto conversationDto = new ConversationDto();
			conversationDto.setId(conversationId);
			conversationDto.setCreatedAt(LocalDateTime.now());
			conversationDto.setUpdatedAt(LocalDateTime.now());

			when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
			when(conversationRepository.findById(conversationId))
					.thenReturn(Optional.of(conversation));
			when(conversationParticipantRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, senderId))
					.thenReturn(true);
			when(conversationParticipantRepository.findByConversationId(conversationId))
					.thenReturn(Arrays.asList(participant));
			when(messageRepository.save(any(Message.class))).thenReturn(message);
			when(messageMapper.toDto(message)).thenReturn(messageDto);
			when(conversationMapper.toDto(conversation)).thenReturn(conversationDto);

			// When - Send message
			MessageDto sentMessage = messageService.sendMessage(sendMessageRequest, senderId);

			// Then - Verify send
			assertNotNull(sentMessage);
			assertEquals(messageId, sentMessage.id());
			verify(eventPublisher).publishEvent(any(NewMessageEvent.class));
			verify(eventPublisher).publishEvent(any(ConversationUpdateEvent.class));

			// Given - Setup for find messages
			LocalDateTime joinedAt = LocalDateTime.now().minusDays(1);
			participant =
					aConversationParticipant()
							.withConversationId(conversationId)
							.withParticipantId(senderId)
							.withActive(true)
							.withJoinedAt(joinedAt)
							.build();

			when(conversationRepository.existsById(conversationId)).thenReturn(true);
			when(conversationParticipantRepository.findByConversationIdAndUserIdAndIsActiveTrue(
							conversationId, senderId))
					.thenReturn(Optional.of(participant));
			when(messageRepository.findByConversationIdAndSentAtAfter(conversationId, joinedAt))
					.thenReturn(Arrays.asList(message));

			// When - Find messages
			List<MessageDto> foundMessages =
					messageService.findConversationMessages(conversationId, senderId);

			// Then - Verify find
			assertNotNull(foundMessages);
			assertEquals(1, foundMessages.size());

			// Given - Setup for delete message
			when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

			// When - Delete message
			messageService.deleteMessage(messageId, senderId);

			// Then - Verify delete
			verify(messageRepository).delete(messageId);
			verify(eventPublisher).publishEvent(any(MessageDeletedEvent.class));
		}
	}
}
