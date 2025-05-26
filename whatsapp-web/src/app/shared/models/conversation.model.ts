import { Message } from './message.model';
import { Participant } from './participant.model';

export interface Conversation {
  id: string; // UUID
  createdAt: string; // ISO Date string
  updatedAt: string; // ISO Date string
  participants: Participant[];
  messages: Message[];
  lastMessage: Message | null;
}
