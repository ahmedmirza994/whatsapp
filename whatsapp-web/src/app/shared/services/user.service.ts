import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/enviroment';
import { ApiResponse } from '../models/api-response.model';
import { User } from '../models/user.model';

@Injectable({
	providedIn: 'root',
})
export class UserService {
	private http = inject(HttpClient);
	private apiUrl = `${environment.apiUrl}/users`;
	constructor() {}

	/**
	 * Searches for users based on a query string.
	 * @param query The search term (name, email, etc.)
	 * @returns Observable<ApiResponse<UserSearch[]>>
	 */
	searchUsers(query: string): Observable<ApiResponse<User[]>> {
		const params = new HttpParams().set('query', query);
		return this.http.get<ApiResponse<User[]>>(`${this.apiUrl}/search`, { params });
	}
}
