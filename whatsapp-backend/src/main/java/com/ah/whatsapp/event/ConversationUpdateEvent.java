package com.ah.whatsapp.event;

import com.ah.whatsapp.dto.ConversationDto;
import org.springframework.context.ApplicationEvent;

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
