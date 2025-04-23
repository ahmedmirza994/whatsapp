import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
	AfterViewChecked,
	Component,
	effect,
	ElementRef,
	inject,
	Input,
	OnDestroy,
	signal,
	ViewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../auth/auth.service';
import { MessageService } from '../../message/message.service';
import { Conversation } from '../../shared/models/conversation.model';
import { Message } from '../../shared/models/message.model';
import { User } from '../../shared/models/user.model';

@Component({
	selector: 'app-conversation-area',
	imports: [CommonModule, FormsModule],
	templateUrl: './conversation-area.component.html',
	styleUrl: './conversation-area.component.css',
})
export class ConversationAreaComponent implements OnDestroy, AfterViewChecked {
	@Input({ required: true }) conversation: Conversation | null = null;
	@ViewChild('messageContainer') private messageContainer!: ElementRef;

	private messageService = inject(MessageService);
	private authService = inject(AuthService); // Inject AuthService

	messages = signal<Message[]>([]);
	isLoading = signal<boolean>(false);
	error = signal<string | null>(null);
	newMessageContent = signal<string>(''); // Signal for new message input
	currentUser = signal<User | null>(null); // Signal for current user

	private shouldScrollToBottom = false;

	constructor() {
		this.currentUser.set(this.authService.loggedInUser); // Get current user

		// Effect to load messages when the conversation input changes
		effect(() => {
			const currentConversation = this.conversation; // Access signal value
			if (currentConversation) {
				this.loadMessages(currentConversation.id);
			} else {
				this.messages.set([]); // Clear messages if no conversation selected
				this.error.set(null);
			}
		});
	}

	ngAfterViewChecked() {
		if (this.shouldScrollToBottom) {
			this.scrollToBottom();
			this.shouldScrollToBottom = false;
		}
	}

	ngOnDestroy(): void {
		// Cleanup if needed (e.g., WebSocket subscriptions later)
	}

	loadMessages(conversationId: string): void {
		this.isLoading.set(true);
		this.error.set(null);
		this.messageService.getConversationMessages(conversationId).subscribe({
			next: msgs => {
				this.messages.set(msgs);
				this.isLoading.set(false);
				this.shouldScrollToBottom = true; // Mark to scroll after view updates
			},
			error: (err: HttpErrorResponse) => {
				console.error('Error loading messages:', err);
				this.error.set('Failed to load messages. Please try again later.');
				this.isLoading.set(false);
			},
		});
	}

	sendMessage(): void {
		const content = this.newMessageContent().trim();
		if (!content || !this.conversation) {
			return;
		}

		console.log(
			'Sending message (HTTP POST - Placeholder):',
			content,
			'to conversation:',
			this.conversation.id
		);
		// ** Placeholder for sending message via HTTP POST **
		// This part will be replaced/enhanced by WebSocket logic later.
		// For now, you could optionally call a service method that uses POST /messages
		// and update the local message list optimistically or upon success.

		// Example optimistic update (remove if calling actual service):
		const tempId = Date.now().toString(); // Temporary ID
		const optimisticMessage: Message = {
			id: tempId,
			conversationId: this.conversation.id,
			senderId: this.currentUser()?.id || 'unknown',
			senderName: this.currentUser()?.name || 'Me',
			content: content,
			sentAt: new Date().toISOString(),
		};
		this.messages.update(msgs => [...msgs, optimisticMessage]);
		this.newMessageContent.set(''); // Clear input
		this.shouldScrollToBottom = true; // Mark to scroll after view updates
	}

	// Helper to get the other participant's name for the header
	getChatPartnerName(): string {
		if (!this.conversation) return '';
		const currentUserId = this.currentUser()?.id;
		const otherParticipant = this.conversation.participants.find(
			p => p.userId !== currentUserId
		);
		return otherParticipant ? otherParticipant.name : 'Chat';
	}

	private scrollToBottom(): void {
		try {
			if (this.messageContainer?.nativeElement) {
				this.messageContainer.nativeElement.scrollTop =
					this.messageContainer.nativeElement.scrollHeight;
			}
		} catch (err) {
			console.error('Could not scroll to bottom:', err);
		}
	}
}
