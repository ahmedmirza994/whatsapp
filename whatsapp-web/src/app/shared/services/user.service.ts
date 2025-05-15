import { HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/enviroment';
import { ApiResponse } from '../models/api-response.model';
import { UserUpdate } from '../models/user-update.model';
import { User } from '../models/user.model';
import { HttpClientService } from './http-client.service';

@Injectable({
	providedIn: 'root',
})
export class UserService {
	private http = inject(HttpClientService);
	private apiUrl = `${environment.apiUrl}/users`;
	private fileApiBaseUrl = `${environment.apiUrl}/files/profile-pictures`;
	constructor() {}

	/**
	 * Searches for users based on a query string.
	 * @param query The search term (name, email, etc.)
	 * @returns Observable<ApiResponse<UserSearch[]>>
	 */
	searchUsers(query: string): Observable<User[]> {
		const params = new HttpParams().set('query', query);
		return this.http.get<User[]>(`${this.apiUrl}/search`, { params }).pipe(
			map(res => {
				return res.data!;
			})
		);
	}

	/**
	 * Fetches the current authenticated user's profile picture.
	 * @returns Observable<Blob> The image data as a Blob.
	 */
	getCurrentUserProfilePicture(): Observable<Blob> {
		return this.http.getBlob(`${this.apiUrl}/me/picture`);
	}

	/**
	 * Updates the current authenticated user's profile details (name, phone).
	 * @param payload The user update data.
	 * @returns Observable<User> The updated user data.
	 */
	updateCurrentUserProfileDetails(payload: UserUpdate): Observable<User> {
		return this.http.put<User>(`${this.apiUrl}/me`, payload).pipe(
			map((response: ApiResponse<User>) => {
				if (!response.data) {
					throw new Error('User data not returned from update profile details');
				}
				return response.data;
			})
		);
	}

	/**
	 * Uploads a new profile picture for the current authenticated user.
	 * @param file The image file to upload.
	 * @returns Observable<User> The updated user data including the new picture URL.
	 */
	uploadCurrentUserProfilePicture(file: File): Observable<User> {
		const formData = new FormData();
		formData.append('file', file, file.name);

		// Note: HttpClientService.post will need to handle FormData correctly,
		// typically by not setting Content-Type header, letting the browser do it.
		return this.http.post<User>(`${this.apiUrl}/me/picture`, formData).pipe(
			map((response: ApiResponse<User>) => {
				if (!response.data) {
					throw new Error('User data not returned from upload profile picture');
				}
				return response.data;
			})
		);
	}

	/**
	 * Constructs the full URL for a user's profile picture.
	 * @param filename The filename of the profile picture.
	 * @returns The full URL string if a filename is provided, otherwise null.
	 */
	getPublicProfilePictureUrl(filename: string | null | undefined): string | null {
		if (!filename) {
			return null;
		}
		return `${this.fileApiBaseUrl}/${filename}`;
	}
}
