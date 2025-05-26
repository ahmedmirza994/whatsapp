import { CommonModule } from '@angular/common';
import { Component, computed, effect, EventEmitter, inject, Input, Output, Signal, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../auth/auth.service'; // Correct path
import { DeleteConfirmationModalComponent } from '../../shared/components/delete-confirmation-modal/delete-confirmation-modal.component';
import { Conversation } from '../../shared/models/conversation.model';
import { Participant } from '../../shared/models/participant.model';
import { getInitial } from '../../shared/models/user.model';
import { EventType, TypingEventPayload, WebSocketEvent } from '../../shared/models/websocket-event.model';
import { UserService } from '../../shared/services/user.service';
import { WebSocketService } from '../../shared/services/websocket.service';
import { ConversationService } from '../conversation.service';
import { NavigationService } from './../../shared/services/navigation.service';

interface ProcessedConversation extends Conversation {
	displayParticipant: Participant;
}

@Component({
	selector: 'app-conversation-list',
	standalone: true,
	imports: [CommonModule, FormsModule, DeleteConfirmationModalComponent],
	templateUrl: './conversation-list.component.html',
	styleUrls: ['./conversation-list.component.css', '../../app.component.css'],
})
export class ConversationListComponent {
	@Input({ required: true }) conversations!: Signal<Conversation[]>;
	@Input({ required: true }) selectedId!: Signal<string | null | undefined>;
	@Output() conversationDeleted = new EventEmitter<string>();

	showDeleteModal = signal(false);
	conversationToDelete: string | null = null;

	typingMap = signal<Record<string, boolean>>({});

	private navigationService = inject(NavigationService);
	private authService = inject(AuthService);
	private userService = inject(UserService);
	private conversationService = inject(ConversationService);
	private webSocketService = inject(WebSocketService);

	currentUserId = this.authService.loggedInUser?.id; // Use signal getter
	searchQuery = signal<string>('');

	constructor() {
		effect(() => {
			this.subscribeToTyping();
		});
	}

	private subscribeToTyping() {
		this.processedConversations().forEach(convo => {
			this.webSocketService.subscribeToTyping(convo.id, (event: WebSocketEvent<TypingEventPayload>) => {
				const data = event.payload;
				if (data.userId !== this.currentUserId) {
					this.typingMap.update(typing => ({
						...typing,
						[convo.id]: event.type === EventType.TYPING_START,
					}));
				}
			});
		});
	}

	private internalFilteredConversations = computed(() => {
		const query = this.searchQuery().toLowerCase().trim();
		const currentConversations = this.conversations(); // Read the signal value here

		if (!query) {
			return currentConversations; // Return all if no query
		}
		return currentConversations.filter(conv => {
			const participantName = this.getOtherParticipant(conv.participants)?.name.toLowerCase();
			return participantName?.includes(query);
		});
	});

	processedConversations: Signal<ProcessedConversation[]> = computed(() => {
		const conversations = this.internalFilteredConversations();
		return conversations.map(conv => {
			return {
				...conv,
				displayParticipant: this.getOtherParticipant(conv.participants),
			};
		});
	});

	onDeleteConversation(id: string, event: MouseEvent): void {
		event.stopPropagation(); // Prevent navigation
		this.conversationToDelete = id;
		this.showDeleteModal.set(true);
	}

	handleDeleteConfirmed() {
		if (this.conversationToDelete) {
			this.conversationService.deleteConversation(this.conversationToDelete).subscribe({
				next: () => {
					this.conversationDeleted.emit(this.conversationToDelete!);
					this.showDeleteModal.set(false);
					this.conversationToDelete = null;
				},
				error: () => {
					alert('Failed to delete conversation.');
					this.showDeleteModal.set(false);
					this.conversationToDelete = null;
				},
			});
		}
	}

	handleDeleteCancelled() {
		this.showDeleteModal.set(false);
		this.conversationToDelete = null;
	}

	onFilterQueryChange(query: string): void {
		console.log('Filter query changed:', query);
		this.searchQuery.set(query);
	}

	clearFilter(): void {
		this.searchQuery.set('');
	}

	selectConversation(id: string): void {
		if (id !== this.selectedId()) {
			this.navigationService.toChat(id);
		}
	}

	getOtherParticipant(participants: Participant[] | undefined): Participant {
		if (!participants || !this.currentUserId) {
			return {
				id: '',
				userId: '',
				name: 'Unknown User',
				profilePicture: null,
				joinedAt: '',
				initial: '?',
				leftAt: null,
				lastReadAt: null,
			};
		}
		const otherParticipants = participants.filter(p => p.userId !== this.currentUserId);
		if (otherParticipants.length > 0) {
			const other = otherParticipants[0];
			return {
				...other,
				initial: getInitial(other.name),
				profilePicture: this.userService.getPublicProfilePictureUrl(other.profilePicture),
			};
		}
		const fallbackName = participants.map(p => p.name ?? 'Unknown').join(', ');
		return {
			id: '',
			userId: '',
			name: fallbackName,
			profilePicture: null,
			joinedAt: '',
			initial: getInitial(fallbackName),
			leftAt: null,
			lastReadAt: null,
		};
	}

	isSeen(conv: ProcessedConversation): boolean {
		const lastReadAt = conv.displayParticipant.lastReadAt;
		const lastMessageSentAt = conv.lastMessage?.sentAt;
		return !!lastReadAt && !!lastMessageSentAt && lastMessageSentAt <= lastReadAt;
	}

	formatTimestamp(timestamp: string | null | undefined): string {
		if (!timestamp) return '';

		try {
			const messageDate = new Date(timestamp);
			const now = new Date();

			// Normalize dates to midnight for comparison
			const messageDay = new Date(messageDate.getFullYear(), messageDate.getMonth(), messageDate.getDate());
			const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
			const yesterday = new Date(today);
			yesterday.setDate(today.getDate() - 1);
			const oneWeekAgo = new Date(today);
			oneWeekAgo.setDate(today.getDate() - 7);

			// Check if it's today
			if (messageDay.getDate() === today.getDate()) {
				return messageDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
			}

			// Check if it's yesterday
			if (messageDay.getDate() === yesterday.getDate()) {
				return 'Yesterday';
			}

			// Check if it's within the last week (but not today or yesterday)
			if (messageDay > oneWeekAgo) {
				return messageDate.toLocaleDateString([], { weekday: 'long' }); // e.g., "Monday"
			}

			// Older than a week
			return messageDate.toLocaleDateString([], {
				year: 'numeric',
				month: 'numeric',
				day: 'numeric',
			}); // e.g., "4/23/2025"
		} catch (e) {
			console.error('Error formatting timestamp:', timestamp, e);
			return ''; // Return empty string on error
		}
	}
}
