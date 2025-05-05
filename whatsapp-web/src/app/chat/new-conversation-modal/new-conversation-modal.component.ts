import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, OnDestroy, OnInit, Output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import {
	debounceTime,
	distinctUntilChanged,
	filter,
	switchMap,
	takeUntil,
	tap,
} from 'rxjs/operators';
import { User } from '../../shared/models/user.model';
import { UserService } from '../../shared/services/user.service';

@Component({
	selector: 'app-new-conversation-modal',
	standalone: true,
	imports: [CommonModule, FormsModule],
	templateUrl: './new-conversation-modal.component.html',
	styleUrls: ['./new-conversation-modal.component.css'],
})
export class NewConversationModalComponent implements OnInit, OnDestroy {
	@Output() closeModal = new EventEmitter<void>();
	@Output() userSelectedToChat = new EventEmitter<User>();

	private userService = inject(UserService);
	private destroy$ = new Subject<void>();
	private searchSubject = new Subject<string>();

	searchQuery = signal<string>('');
	searchResults = signal<User[]>([]);
	isLoading = signal<boolean>(false);
	error = signal<string | null>(null);
	noResultsFound = signal<boolean>(false); // Flag for no results specifically

	ngOnInit(): void {
		this.setupSearchSubscription();
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
	}

	private setupSearchSubscription(): void {
		this.searchSubject
			.pipe(
				debounceTime(300), // Wait for 300ms pause in typing
				distinctUntilChanged(), // Only emit if value changed
				tap(query => {
					this.isLoading.set(query.length > 0); // Show loading only if query is not empty
					this.error.set(null);
					this.searchResults.set([]); // Clear previous results
					this.noResultsFound.set(false); // Reset no results flag
				}),
				filter(query => query.length > 0), // Only search if query is not empty
				switchMap(query =>
					this.userService.searchUsers(query).pipe(
						takeUntil(this.destroy$) // Ensure inner observable is also cleaned up
					)
				),
				takeUntil(this.destroy$) // Main subscription cleanup
			)
			.subscribe({
				next: res => {
					this.searchResults.set(res.data!);
					this.isLoading.set(false);
					this.noResultsFound.set(res.data!.length === 0);
				},
				error: err => {
					console.error('Error searching users:', err);
					this.error.set('Failed to search users. Please try again.');
					this.isLoading.set(false);
					this.noResultsFound.set(false); // Reset flag on error
				},
			});
	}

	onSearchQueryChange(query: string): void {
		this.searchQuery.set(query);
		this.searchSubject.next(query.trim()); // Push the trimmed query to the subject
	}

	selectUser(user: User): void {
		this.userSelectedToChat.emit(user);
	}

	close(): void {
		this.closeModal.emit();
	}

	// Prevent modal close when clicking inside the content
	onModalContentClick(event: MouseEvent): void {
		event.stopPropagation();
	}
}
