/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.event;

import org.springframework.context.ApplicationEvent;

import com.ah.whatsapp.dto.MessageDto;

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
