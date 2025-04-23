import { Component, signal } from '@angular/core';
import { Conversation } from '../../shared/models/conversation.model';
import { ConversationAreaComponent } from '../conversation-area/conversation-area.component';
import { ConversationListComponent } from '../conversation-list/conversation-list.component';

@Component({
	selector: 'app-conversation-main',
	imports: [ConversationListComponent, ConversationAreaComponent],
	templateUrl: './conversation-main.component.html',
	styleUrl: './conversation-main.component.css',
})
export class ConversationMainComponent {
	// Use Angular Signal to track the selected conversation
	selectedConversation = signal<Conversation | null>(null);

	onConversationSelected(conversation: Conversation): void {
		console.log('Conversation selected in main:', conversation);
		this.selectedConversation.set(conversation);
	}
}
