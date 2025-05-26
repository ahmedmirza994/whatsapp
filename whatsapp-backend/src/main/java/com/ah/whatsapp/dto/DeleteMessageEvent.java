/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.dto;

import java.util.UUID;

public record DeleteMessageEvent(UUID messageId, UUID conversationId) {}
