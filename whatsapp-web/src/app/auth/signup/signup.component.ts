import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { QuoteComponent } from '../../quote/quote.component';
import { User } from '../../shared/models/user.model';
import { NavigationService } from '../../shared/services/navigation.service';
import { AuthService } from '../auth.service';
import { SignupRequest } from './signup-request.model';

@Component({
  selector: 'app-signup',
  imports: [CommonModule, ReactiveFormsModule, QuoteComponent],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css'],
})
export class SignupComponent implements OnInit {
  signupForm!: FormGroup;
  errorMessages: string[] = [];
  showPassword: boolean = false;
  isLoading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private navigationService: NavigationService
  ) {}

  ngOnInit() {
    this.signupForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(100)]],
      phone: ['', [Validators.pattern(/^\+[0-9]{7,15}$/)]],
    });
  }

  onSubmit(): void {
    this.errorMessages = [];
    this.isLoading = true;
    if (this.signupForm.valid) {
      const signupRequest: SignupRequest = this.signupForm.value;
      this.authService.signup(signupRequest).subscribe({
        next: (user: User) => {
          console.log('Signup successful', user);
          this.isLoading = false;
        },
        error: (err: Error) => {
          this.errorMessages = err.message.split(',').map(e => e.trim());
          this.isLoading = false;
          console.error('Signup failed', err);
        },
      });
    } else {
      this.isLoading = false;
    }
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  navigateToLogin(): void {
    this.navigationService.toLogin();
  }
}
