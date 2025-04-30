import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { map, switchMap, takeUntil, tap } from 'rxjs/operators';
import { AuthService } from '../../auth/auth.service'; // Import AuthService
import { MessageService } from '../../message/message.service';
import { Conversation } from '../../shared/models/conversation.model'; // Import Conversation model
import { Participant } from '../../shared/models/participant.model'; // Import Participant model
import { ConversationService } from '../conversation.service'; // Import ConversationService
import { MessageAreaComponent } from '../message-area/message-area.component';
import { MessageInputComponent } from '../message-input/message-input.component';

@Component({
	selector: 'app-conversation-detail',
	standalone: true,
	imports: [CommonModule, MessageAreaComponent, MessageInputComponent],
	templateUrl: './conversation-detail.component.html',
	styleUrls: ['./conversation-detail.component.css'],
})
export class ConversationDetailComponent implements OnInit, OnDestroy {
	private route = inject(ActivatedRoute);
	private messageService = inject(MessageService);
	private conversationService = inject(ConversationService); // Inject ConversationService
	private authService = inject(AuthService); // Inject AuthService

	conversationId = signal<string | null>(null);
	conversation = signal<Conversation | null>(null); // Signal to hold conversation details
	participantName = signal<string>('Chat'); // Default name
	participantInitial = signal<string>('?'); // Default initial
	isLoadingHeader = signal<boolean>(false);

	private currentUserId = this.authService.loggedInUser?.id;
	private destroy$ = new Subject<void>();

	ngOnInit(): void {
		this.route.paramMap
			.pipe(
				map(params => params.get('id')),
				tap(id => {
					// Reset state when ID changes
					this.conversationId.set(id);
					this.conversation.set(null);
					this.participantName.set('Loading...');
					this.participantInitial.set('?');
					this.isLoadingHeader.set(true);
					// MessageAreaComponent will react separately via its @Input
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
			.subscribe({
				next: convo => {
					this.conversation.set(convo);
					this.updateParticipantDetails(convo?.participants);
					this.isLoadingHeader.set(false);
				},
				error: err => {
					console.error('Error loading conversation details:', err);
					this.participantName.set('Error');
					this.isLoadingHeader.set(false);
				},
			});
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
	}

	// Helper to get the name and initial of the other participant(s)
	private updateParticipantDetails(participants: Participant[] | undefined): void {
		if (!participants || participants.length === 0) {
			this.participantName.set('Unknown Chat');
			this.participantInitial.set('?');
			return;
		}

		const otherParticipants = participants.filter(p => p.userId !== this.currentUserId);
		let name = 'Unknown';

		if (otherParticipants.length === 0 && participants.length > 0) {
			// Chat with self or group where user is only one listed
			name = participants[0].name ?? 'Yourself';
		} else if (otherParticipants.length === 1) {
			name = otherParticipants[0].name ?? 'Unknown User';
		} else {
			// Group chat with multiple others
			name = otherParticipants.map(p => p.name ?? 'Unknown').join(', ');
		}

		this.participantName.set(name);
		this.participantInitial.set(name?.[0]?.toUpperCase() ?? '?');
	}

	handleSendMessage(content: string): void {
		const convId = this.conversationId();
		if (!convId || !content) return;

		console.log(`Sending message "${content}" to conversation ${convId}`);

		// TODO: Implement actual message sending logic
		// this.messageService.sendMessage(convId, content).subscribe(...)
	}
}
