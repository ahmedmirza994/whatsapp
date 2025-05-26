import { CommonModule } from '@angular/common';
import { AfterViewChecked, Component, effect, ElementRef, inject, Input, OnChanges, OnDestroy, OnInit, signal, SimpleChanges, ViewChild } from '@angular/core';
import { Subject, Subscription } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { AuthService } from '../../auth/auth.service'; // Correct path
import { MessageService } from '../../message/message.service'; // Correct path
import { DeleteConfirmationModalComponent } from '../../shared/components/delete-confirmation-modal/delete-confirmation-modal.component';
import { Message } from '../../shared/models/message.model';
import { EventType, DeleteMessageEventPayload, NewMessageEventPayload, WebSocketEvent } from '../../shared/models/websocket-event.model';
import { WebSocketService } from '../../shared/services/websocket.service';

// Interface for the grouped structure
interface MessageDateGroup {
  dateLabel: string;
  messages: Message[];
}

@Component({
  selector: 'app-message-area',
  standalone: true,
  imports: [CommonModule, DeleteConfirmationModalComponent],
  templateUrl: './message-area.component.html',
  styleUrls: ['./message-area.component.css'],
})
export class MessageAreaComponent implements OnChanges, AfterViewChecked, OnDestroy, OnInit {
  // Receive conversationId as Input
  @Input({ required: true }) conversationId!: string | null;
  @ViewChild('messageList') private messageListContainer!: ElementRef<HTMLDivElement>;

  private messageService = inject(MessageService);
  private authService = inject(AuthService);
  private webSocketService = inject(WebSocketService);

  // Change signal to hold grouped messages
  groupedMessages = signal<MessageDateGroup[]>([]);
  isLoading = signal<boolean>(false);
  error = signal<string | null>(null);
  currentUserId = this.authService.loggedInUser?.id; // Use signal getter

  openMenuForMessageId: string | null = null;
  showDeleteConfirmDialog = false;
  messageToDelete: Message | null = null;
  hoveredMessageId: string | null = null;
  menuButtonHovered = false;
  menuHovered = false;

  private shouldScrollToBottom = false;
  private initialLoadComplete = false;
  private destroy$ = new Subject<void>(); // For unsubscribing
  private wsSubscription: Subscription | null = null;

  constructor() {
    // Effect to scroll when messages change *after* the initial load
    effect(() => {
      const groups = this.groupedMessages(); // Trigger dependency
      // Only scroll if the initial load for *this* conversation is done
      if (this.initialLoadComplete && groups.length > 0) {
        // Check if the last message in the last group is new? More complex scroll logic might be needed
        // For now, scroll whenever groups change after initial load
        this.shouldScrollToBottom = true;
      }
    });
  }

  ngOnInit(): void {
    // Subscribe to incoming WebSocket messages
    this.wsSubscription = this.webSocketService.events$
      .pipe(
        filter((event: WebSocketEvent) => {
          const hasConversationId = (payload: unknown): payload is { conversationId: string } => {
            return typeof payload === 'object' && payload !== null && 'conversationId' in payload;
          };
          return (event.type === EventType.NEW_MESSAGE || event.type === EventType.DELETE_MESSAGE) && hasConversationId(event.payload) && event.payload.conversationId === this.conversationId;
        }),
        takeUntil(this.destroy$)
      )
      .subscribe(event => {
        console.log('WebSocket event received:', event);
        if (event.type === EventType.NEW_MESSAGE) {
          const messagePayload = event.payload as NewMessageEventPayload;
          // Convert payload to Message format
          const newMessage: Message = {
            id: messagePayload.id,
            conversationId: messagePayload.conversationId,
            senderId: messagePayload.senderId,
            senderName: messagePayload.senderName,
            content: messagePayload.content,
            sentAt: messagePayload.sentAt,
          };
          // Add the new message to the signal, ensuring no duplicates
          this.addMessageToGroups(newMessage);
          // Ensure scroll happens for new incoming messages
          this.shouldScrollToBottom = true;
        } else if (event.type === EventType.DELETE_MESSAGE) {
          const deletePayload = event.payload as DeleteMessageEventPayload;
          this.removeMessageFromGroups(deletePayload.messageId);
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
      this.groupedMessages.set([]); // Clear groups
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
    this.wsSubscription?.unsubscribe(); // Unsubscribe from WebSocket events
  }

  loadMessages(convId: string): void {
    this.isLoading.set(true);
    this.error.set(null);
    this.groupedMessages.set([]); // Clear previous groups

    this.messageService
      .getConversationMessages(convId)
      .pipe(takeUntil(this.destroy$)) // Unsubscribe on destroy
      .subscribe({
        next: msgs => {
          // Group messages and set the signal
          this.groupedMessages.set(this.groupMessagesByDate(msgs));
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

  // --- Helper Functions ---

  private groupMessagesByDate(messages: Message[]): MessageDateGroup[] {
    if (!messages || messages.length === 0) {
      return [];
    }

    // 1. Sort messages chronologically (oldest first)
    const sortedMessages = [...messages].sort((a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime());

    const groups: MessageDateGroup[] = [];
    let currentGroup: MessageDateGroup | null = null;

    for (const message of sortedMessages) {
      const messageDate = new Date(message.sentAt);
      const dateLabel = this.formatDateLabel(messageDate);

      if (!currentGroup || currentGroup.dateLabel !== dateLabel) {
        // Start a new group
        currentGroup = { dateLabel: dateLabel, messages: [message] };
        groups.push(currentGroup);
      } else {
        // Add to the existing group
        currentGroup.messages.push(message);
      }
    }

    // 2. Reverse the order of groups so newest date group is first (for column-reverse)
    return groups.reverse();
  }

  private addMessageToGroups(newMessage: Message): void {
    this.groupedMessages.update(currentGroups => {
      const messageDate = new Date(newMessage.sentAt);
      const dateLabel = this.formatDateLabel(messageDate);
      const groupsCopy = [...currentGroups]; // Create a mutable copy

      // Check if the newest group (first in the reversed array) matches the date
      if (groupsCopy.length > 0 && groupsCopy[0].dateLabel === dateLabel) {
        // Add to the newest group, ensuring no duplicates and maintaining order
        const groupMessages = groupsCopy[0].messages;
        if (!groupMessages.some(m => m.id === newMessage.id)) {
          // Insert while maintaining sort (oldest first within group)
          // Since it's a new message, it should usually go at the end
          groupMessages.push(newMessage);
          // Optional: Re-sort just in case of out-of-order arrival, though unlikely for new messages
          // groupMessages.sort((a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime());
        }
      } else {
        // Create a new group for the new date and add it to the beginning (since groups are reversed)
        groupsCopy.unshift({ dateLabel: dateLabel, messages: [newMessage] });
      }
      return groupsCopy; // Return the modified copy
    });
  }

  private formatDateLabel(date: Date): string {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);
    const messageDay = new Date(date.getFullYear(), date.getMonth(), date.getDate());

    if (messageDay.getTime() === today.getTime()) {
      return 'Today';
    }
    if (messageDay.getTime() === yesterday.getTime()) {
      return 'Yesterday';
    }
    // Format for older dates (e.g., "May 1, 2025" or "4/23/2025")
    return date.toLocaleDateString([], {
      year: 'numeric',
      month: 'long', // Use 'long' for full month name
      day: 'numeric',
    });
  }

  scrollToBottom(): void {
    try {
      if (this.messageListContainer?.nativeElement) {
        // Scroll to the bottom (which is visually the top due to column-reverse)
        const element = this.messageListContainer.nativeElement;
        setTimeout(() => {
          // Scroll to the maximum height to reach the bottom
          element.scrollTop = element.scrollHeight;
        }, 0);
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

  closeMessageMenu(): void {
    this.openMenuForMessageId = null;
  }

  showDeleteDialog(msg: Message): void {
    this.messageToDelete = msg;
    this.showDeleteConfirmDialog = true;
    this.closeMessageMenu();
  }

  closeDeleteDialog(): void {
    this.showDeleteConfirmDialog = false;
    this.messageToDelete = null;
  }

  onBubbleHover(messageId: string, isHovering: boolean): void {
    if (isHovering) {
      this.hoveredMessageId = messageId;
    } else {
      // Only clear if menu is not open for this message
      setTimeout(() => {
        if (!this.menuHovered && this.openMenuForMessageId !== messageId) {
          this.hoveredMessageId = null;
        }
      }, 80);
    }
  }

  openMessageMenu(messageId: string): void {
    this.openMenuForMessageId = messageId;
    this.menuHovered = false;
  }

  onMenuLeave(messageId: string): void {
    this.menuHovered = false;
    setTimeout(() => {
      if (this.openMenuForMessageId === messageId && !this.menuHovered) {
        this.openMenuForMessageId = null;
        this.hoveredMessageId = null;
      }
    }, 80);
  }

  shouldShowMenuIcon(messageId: string): boolean {
    return this.hoveredMessageId === messageId || this.openMenuForMessageId === messageId;
  }

  confirmDelete(): void {
    if (this.messageToDelete) {
      this.messageService.deleteMessage(this.messageToDelete.id).subscribe({
        next: () => {
          this.removeMessageFromGroups(this.messageToDelete!.id);
          this.closeDeleteDialog();
        },
        error: () => {
          alert('Failed to delete message.');
          this.closeDeleteDialog();
        },
      });
    }
  }

  private removeMessageFromGroups(messageId: string): void {
    this.groupedMessages.update(groups =>
      groups
        .map(group => ({
          ...group,
          messages: group.messages.filter(m => m.id !== messageId),
        }))
        .filter(group => group.messages.length > 0)
    );
  }
}
