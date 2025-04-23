import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/enviroment';
import { ApiResponse } from '../shared/models/api-response.model';
import { Conversation } from '../shared/models/conversation.model';
import { HttpClientService } from '../shared/services/http-client.service';

@Injectable({
	providedIn: 'root',
})
export class ConversationService {
	private apiUrl = `${environment.apiUrl}/conversations`;

	constructor(private httpClientService: HttpClientService) {}

	/**
	 * Fetches the list of conversations for the current user.
	 */
	getUserConversations(): Observable<Conversation[]> {
		return this.httpClientService
			.get<Conversation[]>(this.apiUrl)
			.pipe(map((response: ApiResponse<Conversation[]>) => response.data ?? []));
	}
}
