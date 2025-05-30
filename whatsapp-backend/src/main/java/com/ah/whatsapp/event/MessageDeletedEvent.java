/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.event;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class MessageDeletedEvent extends ApplicationEvent {
	private final UUID messageId;
	private final UUID conversationId;

	public MessageDeletedEvent(Object source, UUID messageId, UUID conversationId) {
		super(source);
		this.messageId = messageId;
		this.conversationId = conversationId;
	}
}
