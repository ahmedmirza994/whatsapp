import { Routes } from '@angular/router';
import { ChatMainComponent } from './chat-main/chat-main.component';
import { ConversationDetailComponent } from './conversation-detail/conversation-detail.component'; // Import the new component

export const CHAT_ROUTES: Routes = [
	{
		path: '',
		component: ChatMainComponent,
		children: [
			// Define child routes
			{
				path: ':id', // Route parameter for conversation ID
				component: ConversationDetailComponent,
			},
		],
	},
];
