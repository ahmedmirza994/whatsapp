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
	 * Navigate to the home/dashboard page
	 * @param clearHistory When true, replaces current history entry (back button won't return to previous page)
	 */
	toHome(clearHistory: boolean = true): void {
		this.router.navigate(['/'], {
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
