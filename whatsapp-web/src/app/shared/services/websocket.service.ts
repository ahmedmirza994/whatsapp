import { Injectable, OnDestroy, computed, effect, inject, signal } from '@angular/core';
import { Client, IFrame, IMessage, StompSubscription } from '@stomp/stompjs';
import { Subject, filter, first, takeUntil } from 'rxjs';
import SockJS from 'sockjs-client';
import { environment } from '../../../environments/enviroment'; // Adjust path if needed
import { AuthService } from '../../auth/auth.service'; // Adjust path if needed
import { USER_CONVERSATION_QUEUE, WS_ENDPOINT_PATH } from '../constants/websocket.constants';
import { EventType, TypingEventPayload, WebSocketEvent } from '../models/websocket-event.model';

// Define connection states
export enum ConnectionState {
	ATTEMPTING,
	CONNECTED,
	DISCONNECTED,
	ERROR,
}

@Injectable({
	providedIn: 'root',
})
export class WebSocketService implements OnDestroy {
	private authService = inject(AuthService);

	private stompClient?: Client;
	private connectionState = signal<ConnectionState>(ConnectionState.DISCONNECTED);
	private eventSubject = new Subject<WebSocketEvent>();
	private subscriptions: Map<string, StompSubscription> = new Map(); // Store subscriptions by destination
	private pendingSubscriptions: Map<string, boolean> = new Map();
	private destroy$ = new Subject<void>();

	// Public observables/signals
	public connectionState$ = this.connectionState.asReadonly();
	public events$ = this.eventSubject.asObservable();
	private userQueueSubscription: StompSubscription | null = null;

	public isConnected = computed(() => this.connectionState() === ConnectionState.CONNECTED);

	constructor() {
		// Attempt to connect when the user logs in
		this.authService.currentUser$
			.pipe(
				filter(user => !!user), // Only proceed if user is logged in
				first(), // Only connect once per login
				takeUntil(this.destroy$)
			)
			.subscribe(() => {
				this.connect();
			});

		// Disconnect when the user logs out
		this.authService.currentUser$
			.pipe(
				filter(user => !user), // Only proceed if user logs out
				takeUntil(this.destroy$)
			)
			.subscribe(() => {
				this.disconnect();
			});

		// Effect to process pending subscriptions when connection becomes active
		effect(() => {
			if (this.isConnected()) {
				console.log('WebSocket: Connection established, processing pending subscriptions.');
				this.processPendingSubscriptions();
				this.subscribeToUserQueue();
			}
		});
	}

	ngOnDestroy(): void {
		this.disconnect();
		this.destroy$.next();
		this.destroy$.complete();
	}

	private connect(): void {
		if (this.connectionState() === ConnectionState.CONNECTED || this.connectionState() === ConnectionState.ATTEMPTING) {
			console.log('WebSocket: Already connected or attempting.');
			return;
		}

		const token = this.authService.loggedInUser?.jwtToken;
		if (!token) {
			console.error('WebSocket: Cannot connect without JWT token.');
			this.connectionState.set(ConnectionState.ERROR);
			return;
		}

		console.log('WebSocket: Attempting to connect...');
		this.connectionState.set(ConnectionState.ATTEMPTING);

		// Construct the SockJS URL (remove /api prefix if present in environment.apiUrl)
		const wsUrl = environment.apiUrl + WS_ENDPOINT_PATH; // e.g., http://localhost:8080/api/ws

		this.stompClient = new Client({
			// Use SockJS factory
			webSocketFactory: () => new SockJS(wsUrl),
			connectHeaders: {
				Authorization: `Bearer ${token}`, // Pass token in STOMP CONNECT frame
			},
			debug: () => {
				// Optional logging
				// console.log('STOMP: ' + str);
			},
			reconnectDelay: 5000, // Attempt reconnect every 5 seconds
			heartbeatIncoming: 10000, // Expect heartbeats from server
			heartbeatOutgoing: 10000, // Send heartbeats to server
		});

		this.stompClient.onConnect = (frame: IFrame) => {
			console.log('WebSocket: Connected successfully.', frame.command);
			this.connectionState.set(ConnectionState.CONNECTED);
			// Re-subscribe to any topics if needed after reconnection
			this.resubscribeTopics();
		};

		this.stompClient.onStompError = (frame: IFrame) => {
			console.error('WebSocket: Broker reported error: ' + frame.headers['message']);
			console.error('WebSocket: Additional details: ' + frame.body);
			this.connectionState.set(ConnectionState.ERROR);
			// Consider disconnecting or specific error handling
		};

		this.stompClient.onWebSocketError = (event: Event) => {
			console.error('WebSocket: Connection error:', event);
			this.connectionState.set(ConnectionState.ERROR);
		};

		this.stompClient.onDisconnect = (frame: IFrame) => {
			console.log('WebSocket: Disconnected.', frame);
			// Only set to DISCONNECTED if not already attempting a reconnect
			if (this.connectionState() !== ConnectionState.ATTEMPTING) {
				this.connectionState.set(ConnectionState.DISCONNECTED);
			}
		};

		// Activate the client
		this.stompClient.activate();
	}

	/**
	 * Subscribes to the user-specific queue for targeted updates.
	 */
	private subscribeToUserQueue(): void {
		if (!this.stompClient || !this.isConnected()) {
			console.error('WebSocket: Cannot subscribe to user queue. Client not connected.');
			return;
		}
		if (this.userQueueSubscription) {
			console.log('WebSocket: Already subscribed to user queue.');
			return;
		}

		// Destination matches the one used in convertAndSendToUser (without the /user prefix)
		const userQueueDestination = USER_CONVERSATION_QUEUE;
		console.log(`WebSocket: Subscribing to user queue: ${userQueueDestination}`);

		try {
			this.userQueueSubscription = this.stompClient.subscribe(userQueueDestination, (message: IMessage) => {
				try {
					const parsedEvent: WebSocketEvent = JSON.parse(message.body);
					console.log(`WebSocket: Received event from user queue:`, parsedEvent);
					this.eventSubject.next(parsedEvent); // Emit the event
				} catch (e) {
					console.error(`WebSocket: Failed to parse event body from user queue:`, message.body, e);
				}
			});
		} catch (error) {
			console.error(`WebSocket: Error during STOMP subscribe to user queue:`, error);
			this.userQueueSubscription = null; // Reset subscription on error
		}
	}

	public disconnect(): void {
		// Unsubscribe from user queue first if active
		if (this.userQueueSubscription) {
			try {
				this.userQueueSubscription.unsubscribe();
				console.log('WebSocket: Unsubscribed from user queue.');
			} catch (e) {
				console.error('WebSocket: Error unsubscribing from user queue', e);
			} finally {
				this.userQueueSubscription = null;
			}
		}

		if (this.stompClient?.active) {
			console.log('WebSocket: Deactivating client...');
			this.stompClient.deactivate(); // Graceful disconnect
		}
		this.subscriptions.clear(); // Clear stored subscriptions
		this.pendingSubscriptions.clear();
		this.connectionState.set(ConnectionState.DISCONNECTED);
	}

	// Placeholder for resubscribing after reconnect
	private resubscribeTopics(): void {
		this.subscriptions.forEach((_subscription, destination) => {
			console.log(`WebSocket: Re-subscribing to ${destination}`);
			this.subscribeToTopic(destination); // Call the subscribe method again
		});
	}

	private processPendingSubscriptions(): void {
		this.pendingSubscriptions.forEach((_value, destination) => {
			console.log(`WebSocket: Processing pending subscription for ${destination}`);
			this.subscribeToTopicInternal(destination); // Call internal method
		});
		this.pendingSubscriptions.clear(); // Clear the pending list
	}

	/**
	 * Public method to request a subscription. Handles queuing if not connected.
	 * @param destination The topic destination (e.g., /topic/conversations/{id})
	 */
	public subscribeToTopic(destination: string): void {
		if (this.subscriptions.has(destination)) {
			console.log(`WebSocket: Already subscribed to ${destination}.`);
			return;
		}

		if (this.isConnected()) {
			this.subscribeToTopicInternal(destination);
		} else {
			console.log(`WebSocket: Connection not ready for ${destination}. Queuing subscription.`);
			this.pendingSubscriptions.set(destination, true); // Add to pending list
			// Optional: Attempt connection if disconnected? Depends on desired logic.
			// if (this.connectionState() === ConnectionState.DISCONNECTED) {
			//     this.connect();
			// }
		}
	}

	/**
	 * Internal method to perform the actual STOMP subscription. Assumes connection is ready.
	 * @param destination The topic destination.
	 */
	private subscribeToTopicInternal(destination: string): void {
		if (!this.stompClient || !this.isConnected()) {
			console.error(`WebSocket: Internal error - Tried to subscribe to ${destination} while disconnected.`);
			// Add back to pending if connection drops immediately? Or rely on reconnect logic.
			this.pendingSubscriptions.set(destination, true);
			return;
		}
		if (this.subscriptions.has(destination)) {
			console.log(`WebSocket: Internal - Already subscribed to ${destination}.`);
			return;
		}

		console.log(`WebSocket: Subscribing internally to ${destination}`);
		try {
			const subscription = this.stompClient.subscribe(destination, (message: IMessage) => {
				try {
					const parsedEvent: WebSocketEvent = JSON.parse(message.body);
					console.log(`WebSocket: Received message from ${destination}:`, parsedEvent);
					this.eventSubject.next(parsedEvent);
				} catch (e) {
					console.error(`WebSocket: Failed to parse message body from ${destination}:`, message.body, e);
				}
			});
			this.subscriptions.set(destination, subscription); // Store the active subscription
			this.pendingSubscriptions.delete(destination); // Remove from pending if it was there
		} catch (error) {
			console.error(`WebSocket: Error during STOMP subscribe to ${destination}:`, error);
			// Consider adding back to pending or other error handling
		}
	}

	/**
	 * Unsubscribes from a specific topic.
	 * @param destination The topic destination to unsubscribe from.
	 */
	public unsubscribeFromTopic(destination: string): void {
		if (this.pendingSubscriptions.has(destination)) {
			console.log(`WebSocket: Removing pending subscription for ${destination}`);
			this.pendingSubscriptions.delete(destination);
		}
		const subscription = this.subscriptions.get(destination);
		if (subscription) {
			console.log(`WebSocket: Unsubscribing from ${destination}`);
			subscription.unsubscribe();
			this.subscriptions.delete(destination);
		} else {
			console.warn(`WebSocket: No active subscription found for ${destination} to unsubscribe.`);
		}
	}

	/**
	 * Sends a message to a destination via WebSocket.
	 * @param destination The application destination (e.g., /app/chat.sendMessage)
	 * @param payload The message payload object.
	 */
	public sendMessage(destination: string, payload: unknown): void {
		if (!this.isConnected()) {
			// Use computed signal
			console.error(`WebSocket: Cannot send message to ${destination}. Client not connected.`);
			// Optionally queue the message or throw an error
			return;
		}
		console.log(`WebSocket: Sending message to ${destination}:`, payload);
		try {
			this.stompClient?.publish({
				destination: destination,
				body: JSON.stringify(payload),
			});
		} catch (error) {
			console.error(`WebSocket: Error during STOMP publish to ${destination}:`, error);
		}
	}

	sendTypingEvent(conversationId: string, eventType: EventType) {
		const userId = this.authService.loggedInUser?.id;
		if (!userId) return;
		this.stompClient?.publish({
			destination: `/app/typing`,
			body: JSON.stringify({
				conversationId,
				userId,
				eventType,
			}),
		});
	}

	subscribeToTyping(conversationId: string, callback: (event: WebSocketEvent<TypingEventPayload>) => void) {
		const destination = `/topic/conversations/${conversationId}/typing`;
		console.log(`WebSocket: Subscribing to typing events for ${conversationId}`);

		// Use the existing subscription management instead of direct STOMP subscription
		this.subscribeToTopic(destination);

		// Listen to the event stream for typing events on this conversation
		return this.events$
			.pipe(
				filter(
					(event: WebSocketEvent) =>
						(event.type === EventType.TYPING_START || event.type === EventType.TYPING_STOP) && (event.payload as TypingEventPayload)?.conversationId === conversationId
				)
			)
			.subscribe(event => callback(event as WebSocketEvent<TypingEventPayload>));
	}
}
