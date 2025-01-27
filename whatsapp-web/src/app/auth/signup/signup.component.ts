import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
	FormBuilder,
	FormGroup,
	ReactiveFormsModule,
	Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { User } from '../../shared/user.model';
import { AuthService } from '../auth.service';
import { SignupRequest } from './signup-request.model';

@Component({
	selector: 'app-signup',
	imports: [CommonModule, ReactiveFormsModule],
	templateUrl: './signup.component.html',
	styleUrls: ['./signup.component.css'],
})
export class SignupComponent implements OnInit {
	signupForm!: FormGroup;
	errorMessage: string | null = null;

	constructor(
		private fb: FormBuilder,
		private authService: AuthService,
		private router: Router
	) {}

	ngOnInit() {
		this.signupForm = this.fb.group({
			name: [
				'',
				[
					Validators.required,
					Validators.minLength(3),
					Validators.maxLength(100),
				],
			],
			email: ['', [Validators.required, Validators.email]],
			password: [
				'',
				[
					Validators.required,
					Validators.minLength(8),
					Validators.maxLength(100),
				],
			],
			phone: [
				'',
				[Validators.required, Validators.pattern(/^\+[0-9]{7,15}$/)],
			],
		});
	}

	onSubmit(): void {
		if (this.signupForm.valid) {
			const signupRequest: SignupRequest = this.signupForm.value;
			this.authService.signup(signupRequest).subscribe({
				next: (user: User) => {
					console.log('Signup successful', user);
					this.router.navigate(['/']);
				},
				error: (err) => {
					this.errorMessage = err.message;
					console.error('Signup failed', err);
				},
			});
		}
	}
}
