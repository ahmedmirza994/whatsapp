/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.event;

import org.springframework.context.ApplicationEvent;

import com.ah.whatsapp.dto.ConversationDto;

public class ConversationUpdateEvent extends ApplicationEvent {
	private final ConversationDto conversationDto;

	public ConversationUpdateEvent(Object source, ConversationDto conversationDto) {
		super(source);
		this.conversationDto = conversationDto;
	}

	public ConversationDto getConversationDto() {
		return conversationDto;
	}
}
