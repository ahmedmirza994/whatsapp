import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router'; // Import RouterOutlet, ActivatedRoute, NavigationEnd
import { Observable, Subject } from 'rxjs'; // Import Observable
import { filter, map, startWith, takeUntil, tap } from 'rxjs/operators'; // Import operators
import { AuthService } from '../../auth/auth.service'; // Correct path
import { Conversation } from '../../shared/models/conversation.model';
import { User } from '../../shared/models/user.model';
import { EventType, WebSocketEvent } from '../../shared/models/websocket-event.model';
import { WebSocketService } from '../../shared/services/websocket.service';
import { ConversationListComponent } from '../conversation-list/conversation-list.component';
import { ConversationService } from '../conversation.service'; // Correct path

@Component({
	selector: 'app-chat-main',
	standalone: true,
	imports: [
		CommonModule,
		RouterOutlet, // Add RouterOutlet for child routes
		ConversationListComponent,
	],
	templateUrl: './chat-main.component.html',
	styleUrls: ['./chat-main.component.css'],
})
export class ChatMainComponent implements OnInit, OnDestroy {
	private conversationService = inject(ConversationService);
	private authService = inject(AuthService);
	private router = inject(Router);
	private route = inject(ActivatedRoute); // Inject ActivatedRoute
	private webSocketService = inject(WebSocketService);

	conversations = signal<Conversation[]>([]);
	currentUser = signal<User | null>(null);
	error = signal<string | null>(null);
	selectedConversationId = signal<string | null>(null);

	private destroy$ = new Subject<void>();

	// Signal to hold the currently selected conversation ID from the route
	selectedConversationId$: Observable<string | null | undefined> = this.router.events.pipe(
		filter((event): event is NavigationEnd => event instanceof NavigationEnd),
		map(() => this.route.firstChild?.snapshot.paramMap.get('id') ?? null), // Get ID from the child route
		startWith(this.route.firstChild?.snapshot.paramMap.get('id') ?? null),
		tap(id => {
			// Prevent setting signal if value hasn't changed
			if (this.selectedConversationId() !== id) {
				this.selectedConversationId.set(id);
			}
		}),
		takeUntil(this.destroy$) // Get initial value
	);

	ngOnInit(): void {
		this.currentUser.set(this.authService.loggedInUser); // Use signal getter
		if (!this.currentUser()) {
			this.logout();
			return;
		}
		this.loadConversations();
		this.subscribeToConversationUpdates();
		this.selectedConversationId$.subscribe();
	}

	loadConversations(): void {
		this.conversationService
			.getUserConversations()
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: convos => this.conversations.set(convos),
				error: err => {
					console.error('Error loading conversations:', err);
					this.error.set('Failed to load conversations.');
					if (err.status === 401 || err.status === 403) {
						this.logout();
					}
				},
			});
	}

	subscribeToConversationUpdates(): void {
		this.webSocketService.events$
			.pipe(
				filter(
					(event: WebSocketEvent): event is WebSocketEvent<Conversation> =>
						event.type === EventType.CONVERSATION_UPDATE
				),
				takeUntil(this.destroy$)
			)
			.subscribe(event => {
				const updatedConversation = event.payload;
				console.log('ChatMain: Received CONVERSATION_UPDATE event', updatedConversation);
				this.conversations.update(currentConversations => {
					const index = currentConversations.findIndex(
						c => c.id === updatedConversation.id
					);
					let updatedList: Conversation[];
					if (index > -1) {
						// Update existing conversation
						updatedList = [...currentConversations];
						updatedList[index] = updatedConversation;
					} else {
						// Add as a new conversation (should ideally not happen often with this event)
						updatedList = [updatedConversation, ...currentConversations];
					}
					// Re-sort the list to bring the updated one to the top
					return this.sortConversations(updatedList);
				});
			});
	}

	// Helper function to sort conversations by updatedAt descending
	private sortConversations(convos: Conversation[]): Conversation[] {
		return [...convos].sort((a, b) => {
			const dateA = a.updatedAt ? new Date(a.updatedAt).getTime() : 0;
			const dateB = b.updatedAt ? new Date(b.updatedAt).getTime() : 0;
			return dateB - dateA; // Descending order
		});
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
	}

	logout(): void {
		this.authService.logout();
	}
}
