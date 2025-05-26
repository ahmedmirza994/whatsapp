import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/enviroment'; // Correct path if needed
import { Conversation } from '../shared/models/conversation.model';
import { CreateConversationRequest } from '../shared/models/create-conversation-request.model';
import { HttpClientService } from '../shared/services/http-client.service';

@Injectable({
  providedIn: 'root',
})
export class ConversationService {
  private apiUrl = `${environment.apiUrl}/conversations`;
  private httpClientService = inject(HttpClientService);

  getUserConversations(): Observable<Conversation[]> {
    return this.httpClientService.get<Conversation[]>(`${this.apiUrl}`).pipe(map(response => response.data || [])); // Handle potential null data
  }

  // Add method to get a single conversation by ID
  getConversationById(id: string): Observable<Conversation | null> {
    return this.httpClientService.get<Conversation>(`${this.apiUrl}/${id}`).pipe(map(response => response.data || null)); // Return null if not found or error
  }

  findOrCreateConversation(participantId: string): Observable<Conversation> {
    const payload: CreateConversationRequest = { participantId };
    return this.httpClientService.post<Conversation>(`${this.apiUrl}/find-or-create`, payload).pipe(map(response => response.data!)); // Assume data is present
  }

  deleteConversation(convId: string): Observable<string> {
    return this.httpClientService.delete<string>(`${this.apiUrl}/${convId}`).pipe(map(response => response.data || convId));
  }

  markConversationAsRead(convId: string): Observable<string> {
    console.log('Marking conversation as read:', convId);
    return this.httpClientService.post<string>(`${this.apiUrl}/${convId}/read`, {}).pipe(map(response => response.data || convId));
  }
}
