package com.ah.whatsapp.constant;

public class WebSocketConstants {
	private WebSocketConstants() {
    }

	public static final String APP_PREFIX = "/app";
    public static final String CHAT_SEND_MESSAGE = "/chat.sendMessage"; // Relative to APP_PREFIX

    // Broker Destinations (Server -> Client @SendTo / SimpMessagingTemplate)
    public static final String TOPIC_PREFIX = "/topic";
    public static final String CONVERSATIONS_TOPIC = "/conversations"; // Relative to TOPIC_PREFIX
    public static final String CONVERSATION_TOPIC_TEMPLATE = TOPIC_PREFIX + CONVERSATIONS_TOPIC + "/%s"; // Template for specific conversation

    // STOMP Endpoint (Configured in WebSocketConfig, but useful as a constant)
    // Note: This includes the context path '/api' as configured
    public static final String STOMP_ENDPOINT = "/api/ws";
}
