import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router'; // Import RouterOutlet, ActivatedRoute, NavigationEnd
import { Observable } from 'rxjs'; // Import Observable
import { filter, map, startWith } from 'rxjs/operators'; // Import operators
import { AuthService } from '../../auth/auth.service'; // Correct path
import { Conversation } from '../../shared/models/conversation.model';
import { User } from '../../shared/models/user.model';
import { NavigationService } from '../../shared/services/navigation.service';
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
export class ChatMainComponent implements OnInit {
	private conversationService = inject(ConversationService);
	private authService = inject(AuthService);
	private navigationService = inject(NavigationService);
	private router = inject(Router);
	private route = inject(ActivatedRoute); // Inject ActivatedRoute

	conversations = signal<Conversation[]>([]);
	currentUser = signal<User | null>(null);
	error = signal<string | null>(null);

	// Signal to hold the currently selected conversation ID from the route
	selectedConversationId$: Observable<string | null | undefined> = this.router.events.pipe(
		filter((event): event is NavigationEnd => event instanceof NavigationEnd),
		map(() => this.route.firstChild?.snapshot.paramMap.get('id')), // Get ID from the child route
		startWith(this.route.firstChild?.snapshot.paramMap.get('id')) // Get initial value
	);

	ngOnInit(): void {
		this.currentUser.set(this.authService.loggedInUser); // Use signal getter
		if (!this.currentUser()) {
			this.logout();
			return;
		}
		this.loadConversations();
	}

	loadConversations(): void {
		this.conversationService.getUserConversations().subscribe({
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

	onConversationSelected(conversationId: string): void {
		this.navigationService.toChat(conversationId, false);
	}

	logout(): void {
		this.authService.logout();
	}
}
