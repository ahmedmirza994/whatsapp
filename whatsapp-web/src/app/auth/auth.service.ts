import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/enviroment';
import { Observable, map, catchError, throwError } from 'rxjs';
import { User } from '../shared/user.model';
import { SignupRequest } from './signup/signup-request.model';
import { ApiResponse } from '../shared/api-response.model';
import { HttpClientService } from '../shared/http-client.service';

@Injectable({
	providedIn: 'root',
})
export class AuthService {
	private apiUrl = `${environment.apiUrl}/users`;

	constructor(private httpClientService: HttpClientService) {}

	signup(signupRequest: SignupRequest): Observable<User> {
		return this.httpClientService
			.post<User>(`${this.apiUrl}/signup`, signupRequest)
			.pipe(
				map((response) => {
					return response.data!;
				})
			);
	}
}
