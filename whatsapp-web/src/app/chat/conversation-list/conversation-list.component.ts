import { CommonModule } from '@angular/common';
import { Component, Input, inject } from '@angular/core';
import { AuthService } from '../../auth/auth.service'; // Correct path
import { Conversation } from '../../shared/models/conversation.model';
import { Participant } from '../../shared/models/participant.model';
import { NavigationService } from './../../shared/services/navigation.service';

@Component({
	selector: 'app-conversation-list',
	standalone: true,
	imports: [CommonModule],
	templateUrl: './conversation-list.component.html',
	styleUrls: ['./conversation-list.component.css'],
})
export class ConversationListComponent {
	@Input({ required: true }) conversations: Conversation[] = [];
	selectedConversationId: string | null | undefined = null;

	private navigationService = inject(NavigationService);

	private authService = inject(AuthService);
	private currentUserId = this.authService.loggedInUser?.id; // Use signal getter

	selectConversation(id: string): void {
		if (id !== this.selectedConversationId) {
			this.selectedConversationId = id; // Update the selected ID
			this.navigationService.toChat(id);
		}
	}

	getOtherParticipantName(participants: Participant[] | undefined): string {
		if (!participants || participants.length === 0) {
			return 'Unknown';
		}
		const otherParticipants = participants.filter(p => p.userId !== this.currentUserId);
		if (otherParticipants.length === 0 && participants.length > 0)
			return participants[0].name ?? 'Yourself'; // Handle self-chat or groups where user is only participant shown
		if (otherParticipants.length === 1) return otherParticipants[0].name ?? 'Unknown User';
		return otherParticipants.map(p => p.name ?? 'Unknown').join(', '); // Basic group chat display
	}

	formatTimestamp(timestamp: string | null | undefined): string {
		if (!timestamp) return '';

		try {
			const messageDate = new Date(timestamp);
			const now = new Date();

			// Normalize dates to midnight for comparison
			const messageDay = new Date(
				messageDate.getFullYear(),
				messageDate.getMonth(),
				messageDate.getDate()
			);
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
