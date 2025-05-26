// Path for the initial SockJS HTTP connection (relative to environment.apiUrl)
export const WS_ENDPOINT_PATH = '/ws';

// Prefix for messages sent from client to server (@MessageMapping)
export const APP_PREFIX = '/app';

// Prefix for topics subscribed to by the client (Broker destinations)
export const TOPIC_PREFIX = '/topic';

// Specific destination for sending chat messages
export const CHAT_SEND_MESSAGE_DESTINATION = `${APP_PREFIX}/chat.sendMessage`;

// Function to generate the specific topic destination for a conversation
export const getConversationTopicDestination = (conversationId: string): string => {
  return `${TOPIC_PREFIX}/conversations/${conversationId}`;
};

export const USER_CONVERSATION_QUEUE = '/user/queue/conversations';
