export enum EventType {
	NEW_MESSAGE = 'NEW_MESSAGE',
	CONVERSATION_UPDATE = 'CONVERSATION_UPDATE',
}

export interface WebSocketEvent<T = any> {
	type: EventType;
	payload: T;
}
