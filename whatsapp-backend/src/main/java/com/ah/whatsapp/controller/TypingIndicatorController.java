/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.controller;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.ah.whatsapp.dto.TypingIndicatorDto;

import jakarta.validation.Valid;

@Controller
public class TypingIndicatorController {

    private final ApplicationEventPublisher eventPublisher;

    public TypingIndicatorController(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @MessageMapping("/typing")
    public void handleTyping(@Payload @Valid TypingIndicatorDto typingDto) {
        eventPublisher.publishEvent(typingDto);
    }
}
