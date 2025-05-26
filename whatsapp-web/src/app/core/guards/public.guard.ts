import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { NavigationService } from '../../shared/services/navigation.service';

export const publicGuard: CanActivateFn = () => {
	const authService = inject(AuthService);
	const navigationService = inject(NavigationService);

	if (authService.isLoggedIn) {
		console.log('PublicGuard: User already logged in, redirecting to chat.');
		navigationService.toChat(null, true); // Redirect to chat, replace URL
		return false; // Prevent activation of the login/signup route
	} else {
		return true;
	}
};
