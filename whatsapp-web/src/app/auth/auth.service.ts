import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { environment } from '../../environments/enviroment';
import { User } from '../shared/models/user.model';
import { HttpClientService } from '../shared/services/http-client.service';
import { NavigationService } from '../shared/services/navigation.service';
import { LoginRequest } from './login/login-request.model';
import { SignupRequest } from './signup/signup-request.model';

@Injectable({
	providedIn: 'root',
})
export class AuthService {
	private apiUrl = `${environment.apiUrl}/users`;
	private currentUserSubject = new BehaviorSubject<User | null>(null);
	public currentUser$ = this.currentUserSubject.asObservable();

	constructor(
		private httpClientService: HttpClientService,
		private navigationService: NavigationService
	) {
		this.loadUserFromStorage();
	}

	private loadUserFromStorage(): void {
		const storedUser = localStorage.getItem('currentUser');
		if (storedUser) {
			try {
				const user = JSON.parse(storedUser);
				this.currentUserSubject.next(user);
			} catch (error) {
				console.error('Failed to parse stored user data', error);
				localStorage.removeItem('currentUser');
			}
		}
	}

	public get loggedInUser(): User | null {
		return this.currentUserSubject.getValue();
	}

	public get isLoggedIn(): boolean {
		return this.loggedInUser !== null;
	}

	signup(signupRequest: SignupRequest): Observable<User> {
		return this.httpClientService.post<User>(`${this.apiUrl}/signup`, signupRequest).pipe(
			map(response => {
				return response.data!;
			}),
			tap({
				next: user => {
					this.setCurrentUser(user);
					this.navigationService.toChat();
				},
			})
		);
	}

	login(loginRequest: LoginRequest): Observable<User> {
		return this.httpClientService.post<User>(`${this.apiUrl}/login`, loginRequest).pipe(
			map(response => {
				return response.data!;
			}),
			tap({
				next: user => {
					this.setCurrentUser(user);
					this.navigationService.toChat();
				},
			})
		);
	}

	setCurrentUser(user: User): void {
		localStorage.setItem('currentUser', JSON.stringify(user));
		this.currentUserSubject.next(user);
	}

	logout(): void {
		localStorage.removeItem('currentUser');
		this.currentUserSubject.next(null);
		this.navigationService.toLogin();
	}
}
