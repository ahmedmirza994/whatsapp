export enum EventType {
  NEW_MESSAGE = 'NEW_MESSAGE',
  CONVERSATION_UPDATE = 'CONVERSATION_UPDATE',
  DELETE_MESSAGE = 'DELETE_MESSAGE',
  TYPING_START = 'TYPING_START',
  TYPING_STOP = 'TYPING_STOP',
}

// Interfaces for specific event payloads
export interface TypingEventPayload {
  userId: string;
  conversationId: string;
}

export interface MessageEventPayload {
  messageId: string;
  conversationId: string;
  content?: string;
  // Add other message properties as needed
  [key: string]: unknown;
}

export interface NewMessageEventPayload {
  conversationId: string;
  id: string;
  content: string;
  senderId: string;
  senderName: string;
  sentAt: string;
}

export interface DeleteMessageEventPayload {
  messageId: string;
  conversationId: string;
}

export interface WebSocketEvent<T = unknown> {
  type: EventType;
  payload: T;
}
