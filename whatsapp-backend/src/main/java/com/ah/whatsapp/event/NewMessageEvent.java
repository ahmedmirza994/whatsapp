package com.ah.whatsapp.event;

import com.ah.whatsapp.dto.MessageDto;
import org.springframework.context.ApplicationEvent;

public class NewMessageEvent extends ApplicationEvent {
	private final MessageDto messageDto;

	public NewMessageEvent(Object source, MessageDto messageDto) {
		super(source);
		this.messageDto = messageDto;
	}

	public MessageDto getMessageDto() {
		return messageDto;
	}
}
