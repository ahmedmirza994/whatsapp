/*
 * WhatsApp Clone - Backend Service
 * Copyright (c) 2025
 */
package com.ah.whatsapp.exception;

public class MessageNotFoundException extends RuntimeException {
	public MessageNotFoundException(String message) {
		super(message);
	}
}
