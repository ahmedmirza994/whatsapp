import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { NavigationService } from '../../shared/services/navigation.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const navigationService = inject(NavigationService);

  if (authService.isLoggedIn) {
    return true;
  } else {
    // User is not logged in, redirect to login page
    console.log('AuthGuard: User not logged in, redirecting to login.');
    navigationService.toLogin(); // Use the navigation service
    return false; // Deny access to the route
  }
};
