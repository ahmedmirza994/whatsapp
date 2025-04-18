package com.ah.whatsapp.exception;

public class ConversationNotFoundException extends RuntimeException {

	public ConversationNotFoundException(String message) {
		super(message);
	}

}
