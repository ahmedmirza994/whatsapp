import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/enviroment'; // Correct path if needed
import { Conversation } from '../shared/models/conversation.model';
import { HttpClientService } from '../shared/services/http-client.service';

@Injectable({
	providedIn: 'root',
})
export class ConversationService {
	private apiUrl = `${environment.apiUrl}/conversations`;
	private httpClientService = inject(HttpClientService);

	getUserConversations(): Observable<Conversation[]> {
		return this.httpClientService
			.get<Conversation[]>(`${this.apiUrl}`)
			.pipe(map(response => response.data || [])); // Handle potential null data
	}

	// Add method to get a single conversation by ID
	getConversationById(id: string): Observable<Conversation | null> {
		return this.httpClientService
			.get<Conversation>(`${this.apiUrl}/${id}`)
			.pipe(map(response => response.data || null)); // Return null if not found or error
	}

	// Add other methods like createConversation etc. later
}
