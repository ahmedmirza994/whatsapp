import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard'; // Assuming you have an auth guard
import { publicGuard } from './core/guards/public.guard';

export const routes: Routes = [
	{ path: '', redirectTo: '/chat', pathMatch: 'full' },
	{
		path: 'login',
		loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent),
		canActivate: [publicGuard], // Protect the login route
	},
	{
		path: 'signup',
		loadComponent: () => import('./auth/signup/signup.component').then(m => m.SignupComponent),
		canActivate: [publicGuard], // Protect the signup route
	},
	{
		// Chat route loads children
		path: 'chat',
		loadChildren: () => import('./chat/chat.routes').then(m => m.CHAT_ROUTES),
		canActivate: [authGuard], // Protect the chat route
	},
	// Optional: Redirect unmatched routes
	{ path: '**', redirectTo: '/chat' },
];
