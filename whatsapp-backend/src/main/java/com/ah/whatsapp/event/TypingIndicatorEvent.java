package com.ah.whatsapp.event;

import com.ah.whatsapp.dto.TypingIndicatorDto;
import org.springframework.context.ApplicationEvent;

public class TypingIndicatorEvent extends ApplicationEvent {
	private final TypingIndicatorDto typingIndicatorDto;

	public TypingIndicatorEvent(Object source, TypingIndicatorDto typingIndicatorDto) {
		super(source);
		this.typingIndicatorDto = typingIndicatorDto;
	}

	public TypingIndicatorDto getTypingIndicatorDto() {
		return typingIndicatorDto;
	}
}
