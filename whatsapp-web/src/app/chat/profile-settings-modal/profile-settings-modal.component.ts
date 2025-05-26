import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnInit, Output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, finalize, Observable, of, switchMap, tap } from 'rxjs';
import { AuthService } from '../../auth/auth.service';
import { UserUpdate } from '../../shared/models/user-update.model';
import { User } from '../../shared/models/user.model';
import { UserService } from '../../shared/services/user.service';

@Component({
	selector: 'app-profile-settings-modal',
	standalone: true,
	imports: [CommonModule, ReactiveFormsModule],
	templateUrl: './profile-settings-modal.component.html',
	styleUrls: ['./profile-settings-modal.component.css'],
})
export class ProfileSettingsModalComponent implements OnInit {
	@Input() currentUser!: User; // Current user data passed in
	@Input() currentProfilePictureUrl: string | null = null; // Pass the object URL of the current picture

	@Output() closeModal = new EventEmitter<void>();
	@Output() profileUpdated = new EventEmitter<User>(); // Emit updated user

	private fb = inject(FormBuilder);
	private userService = inject(UserService);
	private authService = inject(AuthService);

	profileForm!: FormGroup;
	selectedFile: File | null = null;
	previewUrl: string | ArrayBuffer | null = null;
	isSaving = signal(false);
	errorMessage = signal<string | null>(null);
	successMessage = signal<string | null>(null);

	ngOnInit(): void {
		this.profileForm = this.fb.group({
			name: [this.currentUser?.name || '', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
			phone: [this.currentUser?.phone || '', [Validators.pattern(/^\+[0-9]{7,15}$/)]],
		});
	}

	onFileSelected(event: Event): void {
		const element = event.currentTarget as HTMLInputElement;
		const fileList: FileList | null = element.files;
		if (fileList && fileList[0]) {
			this.selectedFile = fileList[0];
			// Generate preview
			const reader = new FileReader();
			reader.onload = () => {
				this.previewUrl = reader.result;
			};
			reader.readAsDataURL(this.selectedFile);
			this.successMessage.set(null); // Clear previous messages
			this.errorMessage.set(null);
		} else {
			this.selectedFile = null;
			this.previewUrl = null;
		}
	}

	saveProfile(): void {
		if (this.profileForm.invalid && !this.selectedFile) {
			this.errorMessage.set('Form is invalid or no changes made.');
			return;
		}
		this.isSaving.set(true);
		this.errorMessage.set(null);
		this.successMessage.set(null);

		const formValues = this.profileForm.value;
		const profileDetailsChanged = formValues.name !== this.currentUser.name || formValues.phone !== (this.currentUser.phone || '');

		let updateObs: Observable<User | null> = of(null);

		if (profileDetailsChanged) {
			const updatePayload: UserUpdate = {};
			if (formValues.name !== this.currentUser.name) {
				updatePayload.name = formValues.name;
			}
			if (formValues.phone !== (this.currentUser.phone || '')) {
				updatePayload.phone = formValues.phone || null; // Send null if empty
			}
			if (Object.keys(updatePayload).length > 0) {
				updateObs = this.userService.updateCurrentUserProfileDetails(updatePayload);
			}
		}

		updateObs
			.pipe(
				switchMap(updatedUserFromDetails => {
					const userAfterDetailsUpdate = updatedUserFromDetails || this.currentUser;
					if (this.selectedFile) {
						return this.userService.uploadCurrentUserProfilePicture(this.selectedFile).pipe(
							tap(userFromPicUpdate => {
								// If details were also updated, merge results, otherwise use pic update result
								return updatedUserFromDetails
									? {
											...userFromPicUpdate,
											name: userAfterDetailsUpdate.name,
											phone: userAfterDetailsUpdate.phone,
										}
									: userFromPicUpdate;
							})
						);
					}
					return of(userAfterDetailsUpdate); // Return user from detail update or original if no details changed
				}),
				catchError(err => {
					console.error('Error updating profile:', err);
					this.errorMessage.set(err.message || 'Failed to update profile. Please try again.');
					return of(null); // Continue the stream with null on error
				}),
				finalize(() => this.isSaving.set(false))
			)
			.subscribe(finalUpdatedUser => {
				if (finalUpdatedUser && (profileDetailsChanged || this.selectedFile)) {
					this.authService.updateLoggedInUser(finalUpdatedUser); // Update user in AuthService
					this.profileUpdated.emit(finalUpdatedUser);
					this.successMessage.set('Profile updated successfully!');
					this.selectedFile = null; // Reset file input
					this.previewUrl = null;
					// Optionally close modal after a delay or let user close it
					// setTimeout(() => this.closeModal.emit(), 1500);
				} else if (!finalUpdatedUser && !this.errorMessage()) {
					if (!profileDetailsChanged && !this.selectedFile) {
						this.errorMessage.set('No changes were made.');
					}
				}
			});
	}

	triggerCloseModal(): void {
		this.closeModal.emit();
	}
}
