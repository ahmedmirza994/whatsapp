package com.ah.whatsapp.event;

import java.util.UUID;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

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
