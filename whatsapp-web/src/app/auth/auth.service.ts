import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/enviroment';
import { Observable, map, catchError, throwError } from 'rxjs';
import { User } from '../shared/user.model';
import { SignupRequest } from './signup/signup-request.model';
import { ApiResponse } from '../shared/api-response.model';

@Injectable({
	providedIn: 'root',
})
export class AuthService {
	private apiUrl = `${environment.apiUrl}/users`;

	constructor(private http: HttpClient) {}

	signup(signupRequest: SignupRequest): Observable<User> {
		return this.http
			.post<ApiResponse<User>>(`${this.apiUrl}/signup`, signupRequest)
			.pipe(
				map((response) => {
					if (response.status === 200 && response.data) {
						return response.data;
					} else {
						throw new Error(response.error || 'Signup failed');
					}
				}),
				catchError((error) => {
					console.error('Signup error', error);
					return throwError(
						() => new Error(error.message || 'Signup failed')
					);
				})
			);
	}
}
