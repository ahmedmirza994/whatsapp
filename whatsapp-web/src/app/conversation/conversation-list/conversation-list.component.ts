import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, inject, OnInit, Output, signal } from '@angular/core';
import { AuthService } from '../../auth/auth.service';
import { Conversation } from '../../shared/models/conversation.model';
import { User } from '../../shared/models/user.model';
import { ConversationService } from '../conversation.service';

@Component({
	selector: 'app-conversation-list',
	imports: [CommonModule],
	templateUrl: './conversation-list.component.html',
	styleUrl: './conversation-list.component.css',
})
export class ConversationListComponent implements OnInit {
	private conversationService = inject(ConversationService);
	private authService = inject(AuthService); // Inject AuthService

	conversations = signal<Conversation[]>([]);
	isLoading = signal<boolean>(true);
	error = signal<string | null>(null);
	selectedConversationId = signal<string | null>(null);
	currentUser = signal<User | null>(null); // Signal for current user

	// Output event emitter for when a conversation is selected
	@Output() conversationSelected = new EventEmitter<Conversation>();

	ngOnInit(): void {
		this.currentUser.set(this.authService.loggedInUser); // Get current user
		this.loadConversations();
	}

	loadConversations(): void {
		this.isLoading.set(true);
		this.error.set(null);
		this.conversationService.getUserConversations().subscribe({
			next: convos => {
				this.conversations.set(convos);
				this.isLoading.set(false);
			},
			error: (err: HttpErrorResponse) => {
				console.error('Error loading conversations:', err);
				this.error.set('Failed to load conversations. Please try again later.');
				this.isLoading.set(false);
			},
		});
	}

	selectConversation(conversation: Conversation): void {
		this.selectedConversationId.set(conversation.id);
		this.conversationSelected.emit(conversation); // Emit the selected conversation
	}

	getOtherParticipantName(conversation: Conversation): string {
		const currentUserId = this.currentUser()?.id;
		const otherParticipant = conversation.participants.find(p => p.userId !== currentUserId);
		return otherParticipant ? otherParticipant.name : 'Unknown User';
	}

	// Helper to get a preview of the last message
	getLastMessagePreview(conversation: Conversation): string {
		if (!conversation.lastMessage) {
			return 'No messages yet';
		}
		const maxLength = 30; // Max length for preview
		const content = conversation.lastMessage.content;
		return content.length > maxLength ? content.substring(0, maxLength) + '...' : content;
	}
}
