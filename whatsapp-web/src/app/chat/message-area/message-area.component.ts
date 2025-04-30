import { CommonModule } from '@angular/common';
import {
	AfterViewChecked,
	Component,
	effect,
	ElementRef,
	inject,
	Input,
	OnChanges,
	OnDestroy,
	signal,
	SimpleChanges,
	ViewChild,
} from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../auth/auth.service'; // Correct path
import { MessageService } from '../../message/message.service'; // Correct path
import { Message } from '../../shared/models/message.model';

@Component({
	selector: 'app-message-area',
	standalone: true,
	imports: [CommonModule],
	templateUrl: './message-area.component.html',
	styleUrls: ['./message-area.component.css'],
})
export class MessageAreaComponent implements OnChanges, AfterViewChecked, OnDestroy {
	// Receive conversationId as Input
	@Input({ required: true }) conversationId!: string | null;
	@ViewChild('messageList') private messageListContainer!: ElementRef<HTMLDivElement>;

	private messageService = inject(MessageService);
	private authService = inject(AuthService);

	messages = signal<Message[]>([]);
	isLoading = signal<boolean>(false);
	error = signal<string | null>(null);
	currentUserId = this.authService.loggedInUser?.id; // Use signal getter

	private shouldScrollToBottom = false;
	private initialLoadComplete = false;
	private destroy$ = new Subject<void>(); // For unsubscribing

	constructor() {
		// Effect to scroll when messages change *after* the initial load
		effect(() => {
			const currentMessages = this.messages(); // Trigger dependency
			// Only scroll if the initial load for *this* conversation is done
			if (this.initialLoadComplete && currentMessages.length > 0) {
				this.shouldScrollToBottom = true;
			}
		});
	}

	ngOnChanges(changes: SimpleChanges): void {
		// React to conversationId changes from the parent (ConversationDetailComponent)
		if (changes['conversationId'] && this.conversationId) {
			this.initialLoadComplete = false; // Reset flag on conversation change
			this.loadMessages(this.conversationId);
		} else if (changes['conversationId'] && !this.conversationId) {
			// Handle case where conversationId becomes null (e.g., navigating away)
			this.messages.set([]);
			this.error.set(null);
			this.isLoading.set(false);
		}
	}

	ngAfterViewChecked(): void {
		if (this.shouldScrollToBottom) {
			this.scrollToBottom();
			this.shouldScrollToBottom = false;
		}
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
	}

	loadMessages(convId: string): void {
		this.isLoading.set(true);
		this.error.set(null);
		this.messages.set([]); // Clear previous messages

		this.messageService
			.getConversationMessages(convId)
			.pipe(takeUntil(this.destroy$)) // Unsubscribe on destroy
			.subscribe({
				next: msgs => {
					// Ensure messages are sorted chronologically before setting signal
					this.messages.set(
						msgs.sort(
							(a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime()
						)
					);
					this.isLoading.set(false);
					this.initialLoadComplete = true;
					// Ensure scroll happens after the very first load completes
					if (msgs.length > 0) {
						this.shouldScrollToBottom = true;
					}
				},
				error: err => {
					console.error(`Error loading messages for ${convId}:`, err);
					this.error.set('Failed to load messages.');
					this.isLoading.set(false);
					this.initialLoadComplete = true;
				},
			});
	}

	scrollToBottom(): void {
		try {
			if (this.messageListContainer?.nativeElement) {
				// Scroll to the bottom (which is visually the top due to column-reverse)
				this.messageListContainer.nativeElement.scrollTop = 0;
			}
		} catch (err) {
			console.error('Could not scroll to bottom:', err);
		}
	}

	formatTimestamp(timestamp: string | null | undefined): string {
		if (!timestamp) return '';
		try {
			const date = new Date(timestamp);
			return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
		} catch (e) {
			console.error('Error formatting timestamp:', timestamp, e);
			return '';
		}
	}
}
