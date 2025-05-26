import { CommonModule } from '@angular/common';
import { Component, effect, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router'; // Import RouterOutlet, ActivatedRoute, NavigationEnd
import { Observable, Subject, Subscription } from 'rxjs'; // Import Observable
import { filter, map, startWith, takeUntil } from 'rxjs/operators'; // Import operators
import { AuthService } from '../../auth/auth.service'; // Correct path
import { Conversation } from '../../shared/models/conversation.model';
import { getInitial, User } from '../../shared/models/user.model';
import { EventType, WebSocketEvent } from '../../shared/models/websocket-event.model';
import { NavigationService } from '../../shared/services/navigation.service';
import { UserService } from '../../shared/services/user.service';
import { WebSocketService } from '../../shared/services/websocket.service';
import { ConversationListComponent } from '../conversation-list/conversation-list.component';
import { ConversationService } from '../conversation.service';
import { NewConversationModalComponent } from '../new-conversation-modal/new-conversation-modal.component';
import { ProfileSettingsModalComponent } from '../profile-settings-modal/profile-settings-modal.component'; // Correct path

@Component({
  selector: 'app-chat-main',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet, // Add RouterOutlet for child routes
    ConversationListComponent,
    NewConversationModalComponent,
    ProfileSettingsModalComponent,
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
  private navigationService = inject(NavigationService);
  private userService = inject(UserService);

  conversations = signal<Conversation[]>([]);
  currentUser = signal<User | null>(null);
  error = signal<string | null>(null);
  showNewConversationModal = signal<boolean>(false);
  showProfileSettingsModal = signal(false);

  profilePictureObjectUrl = signal<string | null>(null); // Signal for the profile picture object URL
  private previousBlobUrl: string | null = null;
  private subscriptions = new Subscription();

  private destroy$ = new Subject<void>();

  // Signal to hold the currently selected conversation ID from the route
  selectedConversationId$: Observable<string | null | undefined> = this.router.events.pipe(
    filter((event): event is NavigationEnd => event instanceof NavigationEnd),
    map(() => this.route.firstChild?.snapshot.paramMap.get('id') ?? null), // Get ID from the child route
    startWith(this.route.firstChild?.snapshot.paramMap.get('id') ?? null)
  );

  selectedConversationIdSignal = toSignal(this.selectedConversationId$, {
    initialValue: this.route.firstChild?.snapshot.paramMap.get('id') ?? null, // Provide initial value
    // destroyRef: this.destroyRef // Automatically unsubscribes
  });

  constructor() {
    effect(() => {
      const user = this.currentUser();
      if (user && user.profilePicture) {
        this.subscriptions.add(
          this.userService.getCurrentUserProfilePicture().subscribe({
            next: blob => {
              if (this.previousBlobUrl) {
                URL.revokeObjectURL(this.previousBlobUrl);
              }
              const newUrl = URL.createObjectURL(blob);
              this.profilePictureObjectUrl.set(newUrl);
              this.previousBlobUrl = newUrl;
            },
            error: err => {
              console.error('Error loading profile picture:', err);
              if (this.previousBlobUrl) {
                URL.revokeObjectURL(this.previousBlobUrl);
                this.previousBlobUrl = null;
              }
              this.profilePictureObjectUrl.set(null);
            },
          })
        );
      } else {
        if (this.previousBlobUrl) {
          URL.revokeObjectURL(this.previousBlobUrl);
          this.previousBlobUrl = null;
        }
        this.profilePictureObjectUrl.set(null);
      }
    });
  }

  ngOnInit(): void {
    this.currentUser.set(this.authService.loggedInUser); // Use signal getter
    if (!this.currentUser()) {
      this.logout();
      return;
    }
    this.loadConversations();
    this.subscribeToConversationUpdates();
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
        filter((event: WebSocketEvent): event is WebSocketEvent<Conversation> => event.type === EventType.CONVERSATION_UPDATE),
        takeUntil(this.destroy$)
      )
      .subscribe(event => {
        const updatedConversation = event.payload;
        console.log('ChatMain: Received CONVERSATION_UPDATE event', updatedConversation);
        this.conversations.update(currentConversations => {
          const index = currentConversations.findIndex(c => c.id === updatedConversation.id);
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

  handleConversationDeleted(conversationId: string): void {
    this.conversations.update(list => list.filter(c => c.id !== conversationId));
    if (this.selectedConversationIdSignal() === conversationId) {
      this.navigationService.toChat(null);
    }
  }

  openNewConversationModal(): void {
    this.showNewConversationModal.set(true);
  }

  // Method to close the modal
  closeNewConversationModal(): void {
    this.showNewConversationModal.set(false);
  }

  openProfileSettingsModal(): void {
    this.showProfileSettingsModal.set(true);
  }

  closeProfileSettingsModal(): void {
    this.showProfileSettingsModal.set(false);
  }

  startChatWithUser(user: User): void {
    console.log('ChatMain: User selected from modal, starting chat with:', user);
    this.closeNewConversationModal(); // Close modal after selection

    const existingConvo = this.conversations().find(convo => convo.participants.some(p => p.userId === user.id && convo.participants.length === 2));

    if (existingConvo) {
      console.log('Navigating to existing conversation:', existingConvo.id);
      this.navigationService.toChat(existingConvo.id);
    } else {
      console.log('Attempting to find or create conversation for user:', user.id);
      // Call backend service to find/create conversation
      this.conversationService.findOrCreateConversation(user.id).subscribe({
        next: convo => {
          console.log('Found or created conversation:', convo);
          // Update conversation list locally or reload
          this.loadConversations(); // Simple reload for now to ensure list is up-to-date
          // Navigate to the new/existing chat
          this.navigationService.toChat(convo.id);
        },
        error: err => {
          console.error('Error finding or creating conversation:', err);
          this.error.set('Could not start conversation.'); // Show error feedback
        },
      });
    }
  }

  // Helper function to sort conversations by updatedAt descending
  private sortConversations(convos: Conversation[]): Conversation[] {
    return [...convos].sort((a, b) => {
      const dateA = a.updatedAt ? new Date(a.updatedAt).getTime() : 0;
      const dateB = b.updatedAt ? new Date(b.updatedAt).getTime() : 0;
      return dateB - dateA; // Descending order
    });
  }

  getInitial(name: string) {
    return getInitial(name);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  handleProfileUpdated(updatedUser: User): void {
    this.currentUser.set(updatedUser); // Update the current user signal
    this.closeProfileSettingsModal();
  }

  logout(): void {
    this.authService.logout();
    this.subscriptions.unsubscribe();
    if (this.previousBlobUrl) {
      URL.revokeObjectURL(this.previousBlobUrl);
    }
  }
}
