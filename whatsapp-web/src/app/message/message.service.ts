import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/enviroment';
import { ApiResponse } from '../shared/models/api-response.model';
import { Message } from '../shared/models/message.model';
import { HttpClientService } from '../shared/services/http-client.service';

@Injectable({
	providedIn: 'root',
})
export class MessageService {
	private apiUrl = `${environment.apiUrl}/messages`;

	constructor(private httpClientService: HttpClientService) {}

	/**
	 * Fetches messages for a specific conversation.
	 * @param conversationId The ID of the conversation.
	 */
	getConversationMessages(conversationId: string): Observable<Message[]> {
		return this.httpClientService.get<Message[]>(`${this.apiUrl}/conversation/${conversationId}`).pipe(map((response: ApiResponse<Message[]>) => response.data ?? []));
	}

	deleteMessage(messageId: string): Observable<string> {
		return this.httpClientService.delete<string>(`${this.apiUrl}/${messageId}`).pipe(map((response: ApiResponse<string>) => response.data || messageId));
	}
}
