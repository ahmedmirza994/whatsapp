export enum EventType {
	NEW_MESSAGE = 'NEW_MESSAGE',
	CONVERSATION_UPDATE = 'CONVERSATION_UPDATE',
	DELETE_MESSAGE = 'DELETE_MESSAGE',
	TYPING_START = 'TYPING_START',
	TYPING_STOP = 'TYPING_STOP',
}

export interface WebSocketEvent<T = any> {
	type: EventType;
	payload: T;
}
