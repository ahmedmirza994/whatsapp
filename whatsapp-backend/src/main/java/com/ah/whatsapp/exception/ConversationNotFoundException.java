/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.exception;

public class ConversationNotFoundException extends RuntimeException {

    public ConversationNotFoundException(String message) {
        super(message);
    }
}
