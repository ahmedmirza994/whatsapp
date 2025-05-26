/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.event;

import org.springframework.context.ApplicationEvent;

import com.ah.whatsapp.dto.TypingIndicatorDto;

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
