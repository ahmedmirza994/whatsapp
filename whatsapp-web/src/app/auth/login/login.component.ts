import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { QuoteComponent } from '../../quote/quote.component';
import { User } from '../../shared/models/user.model';
import { NavigationService } from '../../shared/services/navigation.service';
import { AuthService } from '../auth.service';
import { LoginRequest } from './login-request.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, QuoteComponent],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  errorMessages: string[] = [];
  showPassword: boolean = false;
  isLoading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private navigationService: NavigationService
  ) {}

  ngOnInit() {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
    });
  }

  onSubmit(): void {
    this.errorMessages = [];
    this.isLoading = true;
    if (this.loginForm.valid) {
      const loginRequest: LoginRequest = this.loginForm.value;
      this.authService.login(loginRequest).subscribe({
        next: (user: User) => {
          console.log('Login successful', user);
          this.isLoading = false;
        },
        error: (err: Error) => {
          this.errorMessages = err.message.split(',').map(e => e.trim());
          this.isLoading = false;
          console.error('Login failed', err);
        },
      });
    } else {
      this.isLoading = false;
    }
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  navigateToSignup(): void {
    this.navigationService.toSignup();
  }
}
