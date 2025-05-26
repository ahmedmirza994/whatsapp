import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class NavigationService {
  constructor(private router: Router) {}

  /**
   * Navigate to the login page
   */
  toLogin(): void {
    this.router.navigate(['/login']);
  }

  /**
   * Navigate to the signup page
   */
  toSignup(): void {
    this.router.navigate(['/signup']);
  }

  /**
   * Navigate to the chat view, optionally to a specific conversation.
   * @param conversationId The ID of the conversation to navigate to. If null/undefined, navigates to the base chat view.
   * @param clearHistory If true, replaces the current entry in the browser history. Defaults to false.
   */
  toChat(conversationId?: string | null, clearHistory: boolean = false): void {
    const commands = conversationId ? ['/chat', conversationId] : ['/chat'];
    this.router.navigate(commands, {
      replaceUrl: clearHistory,
    });
  }

  /**
   * Navigate back in the browser history
   */
  back(): void {
    window.history.back();
  }
}
