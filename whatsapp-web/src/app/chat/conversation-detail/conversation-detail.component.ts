import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subject, Subscription } from 'rxjs';
import { distinctUntilChanged, map, switchMap, takeUntil, tap } from 'rxjs/operators';
import { AuthService } from '../../auth/auth.service'; // Import AuthService
import { CHAT_SEND_MESSAGE_DESTINATION, getConversationTopicDestination } from '../../shared/constants/websocket.constants';
import { Participant } from '../../shared/models/participant.model'; // Import Participant model
import { SendMessageRequest } from '../../shared/models/send-message-request.model';
import { getInitial } from '../../shared/models/user.model';
import { EventType, TypingEventPayload, WebSocketEvent } from '../../shared/models/websocket-event.model';
import { UserService } from '../../shared/services/user.service';
import { WebSocketService } from '../../shared/services/websocket.service';
import { ConversationService } from '../conversation.service'; // Import ConversationService
import { MessageAreaComponent } from '../message-area/message-area.component';
import { MessageInputComponent } from '../message-input/message-input.component';

@Component({
	selector: 'app-conversation-detail',
	standalone: true,
	imports: [CommonModule, MessageAreaComponent, MessageInputComponent],
	templateUrl: './conversation-detail.component.html',
	styleUrls: ['./conversation-detail.component.css', '../../app.component.css'],
})
export class ConversationDetailComponent implements OnInit, OnDestroy {
	private route = inject(ActivatedRoute);
	private conversationService = inject(ConversationService); // Inject ConversationService
	private authService = inject(AuthService); // Inject AuthService
	private webSocketService = inject(WebSocketService);
	private userService = inject(UserService); // Inject UserService

	conversationId = signal<string | null>(null);
	participant = signal<Participant | null>(null);
	isLoadingHeader = signal<boolean>(false);

	private currentUserId = this.authService.loggedInUser?.id;
	private currentSubscriptionDestination: string | null = null;
	private destroy$ = new Subject<void>();

	isTyping = signal<boolean>(false);
	private typingSubscription: Subscription | undefined;

	ngOnInit(): void {
		this.route.paramMap
			.pipe(
				map(params => params.get('id')),
				distinctUntilChanged(),
				tap(id => {
					// Unsubscribe from the previous topic before processing the new ID
					if (this.currentSubscriptionDestination) {
						this.webSocketService.unsubscribeFromTopic(this.currentSubscriptionDestination);
						this.currentSubscriptionDestination = null;
					}

					// Reset state when ID changes
					this.conversationId.set(id);
					this.isLoadingHeader.set(true);

					// Subscribe to the new topic if an ID exists
					if (id) {
						this.currentSubscriptionDestination = getConversationTopicDestination(id);
						this.webSocketService.subscribeToTopic(this.currentSubscriptionDestination);
					}
				}),
				// Only proceed if ID is not null
				switchMap(id => {
					if (!id) {
						this.isLoadingHeader.set(false);
						return []; // Return empty observable if no ID
					}
					// Fetch conversation details based on the ID
					return this.conversationService.getConversationById(id);
				}),
				takeUntil(this.destroy$)
			)
			.pipe(
				tap(conv => {
					if (conv && conv.lastMessage && conv.lastMessage.senderId !== this.currentUserId) {
						this.conversationService.markConversationAsRead(conv.id).subscribe();
					}
				})
			)
			.subscribe({
				next: convo => {
					this.updateParticipantDetails(convo?.participants);
					this.isLoadingHeader.set(false);
				},
				error: err => {
					console.error('Error loading conversation details:', err);
					this.isLoadingHeader.set(false);
				},
			});

		// Subscribe to typing events for the current conversation
		const currentConvId = this.conversationId();
		if (currentConvId) {
			this.typingSubscription = this.webSocketService.subscribeToTyping(currentConvId, (event: WebSocketEvent<TypingEventPayload>) => {
				const data = event.payload;
				if (data.userId !== this.currentUserId) {
					if (event.type === EventType.TYPING_START) {
						this.isTyping.set(true);
					} else if (event.type === EventType.TYPING_STOP) {
						this.isTyping.set(false);
					}
				}
			});
		}
	}

	ngOnDestroy(): void {
		// Unsubscribe from the current topic when component is destroyed
		if (this.currentSubscriptionDestination) {
			this.webSocketService.unsubscribeFromTopic(this.currentSubscriptionDestination);
		}
		this.destroy$.next();
		this.destroy$.complete();
		this.typingSubscription?.unsubscribe();
	}

	// Helper to get the name and initial of the other participant(s)
	private updateParticipantDetails(participants: Participant[] | undefined): void {
		let participant = null;
		if (!participants || participants.length === 0) {
			participant = null;
			return;
		}

		const otherParticipants = participants.filter(p => p.userId !== this.currentUserId);

		if (otherParticipants.length === 0 && participants.length > 0) {
			// Only the current user is present
			participant = participants[0];
		} else if (otherParticipants.length === 1) {
			participant = otherParticipants[0];
		} else {
			participant = otherParticipants[0];
		}

		participant = {
			...participant,
			initial: getInitial(participant.name),
			profilePicture: this.userService.getPublicProfilePictureUrl(participant.profilePicture),
		};
		this.participant.set(participant);
	}

	handleSendMessage(content: string): void {
		const convId = this.conversationId();
		if (!convId || !content || !this.currentUserId) {
			console.error('Cannot send message: Missing conversation ID, content, or user ID.');
			return;
		}

		console.log(`Sending message "${content}" to conversation ${convId}`);

		// Create the payload matching the backend DTO
		const payload: SendMessageRequest = {
			conversationId: convId,
			content: content,
		};

		// Send message via WebSocketService
		this.webSocketService.sendMessage(CHAT_SEND_MESSAGE_DESTINATION, payload);
	}
}
