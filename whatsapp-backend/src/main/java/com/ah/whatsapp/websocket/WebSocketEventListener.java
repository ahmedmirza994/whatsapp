/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.websocket;

import static com.ah.whatsapp.constant.WebSocketConstants.CONVERSATION_QUEUE;
import static com.ah.whatsapp.constant.WebSocketConstants.CONVERSATION_TOPIC_TEMPLATE;
import static com.ah.whatsapp.constant.WebSocketConstants.TYPING_INDICATOR_TOPIC;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.ah.whatsapp.dto.ConversationDto;
import com.ah.whatsapp.dto.DeleteMessageEvent;
import com.ah.whatsapp.dto.MessageDto;
import com.ah.whatsapp.dto.TypingIndicatorDto;
import com.ah.whatsapp.dto.WebSocketEvent;
import com.ah.whatsapp.enums.EventType;
import com.ah.whatsapp.event.ConversationUpdateEvent;
import com.ah.whatsapp.event.MessageDeletedEvent;
import com.ah.whatsapp.event.NewMessageEvent;

@Component
public class WebSocketEventListener {
	private final SimpMessagingTemplate messagingTemplate;

	public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	@Async
	@EventListener
	public void handleNewMessage(NewMessageEvent event) {
		MessageDto messageDto = event.getMessageDto();
		WebSocketEvent<MessageDto> wsEvent =
				new WebSocketEvent<>(EventType.NEW_MESSAGE, messageDto);
		String destination =
				String.format(CONVERSATION_TOPIC_TEMPLATE, messageDto.conversationId());
		messagingTemplate.convertAndSend(destination, wsEvent);
	}

	@Async
	@EventListener
	public void handleConversationUpdate(ConversationUpdateEvent event) {
		ConversationDto conversationDto = event.getConversationDto();
		WebSocketEvent<ConversationDto> wsEvent =
				new WebSocketEvent<>(EventType.CONVERSATION_UPDATE, conversationDto);
		// Send to each participant's queue
		conversationDto
				.getParticipants()
				.forEach(
						participant -> {
							String email = participant.email();
							if (email != null) {
								messagingTemplate.convertAndSendToUser(
										email, CONVERSATION_QUEUE, wsEvent);
							}
						});
	}

	@Async
	@EventListener
	public void handleMessageDeleted(MessageDeletedEvent event) {
		WebSocketEvent<DeleteMessageEvent> webSocketEvent =
				new WebSocketEvent<>(
						EventType.DELETE_MESSAGE,
						new DeleteMessageEvent(event.getMessageId(), event.getConversationId()));
		String destination = String.format(CONVERSATION_TOPIC_TEMPLATE, event.getConversationId());
		messagingTemplate.convertAndSend(destination, webSocketEvent);
	}

	@Async
	@EventListener
	public void handleTypingIndicator(TypingIndicatorDto typingDto) {
		WebSocketEvent<TypingIndicatorDto> wsEvent =
				new WebSocketEvent<>(typingDto.getEventType(), typingDto);
		String destination = String.format(TYPING_INDICATOR_TOPIC, typingDto.getConversationId());
		messagingTemplate.convertAndSend(destination, wsEvent);
	}
}
