export interface Message {
  id: string; // UUID
  conversationId: string; // UUID
  senderId: string; // UUID
  senderName: string;
  content: string;
  sentAt: string; // ISO Date string
}
