import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { SignupComponent } from './auth/signup/signup.component';
import { ConversationMainComponent } from './conversation/conversation-main/conversation-main.component';
import { AuthGuard } from './core/guards/auth.guard';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{ path: 'signup', component: SignupComponent },
	{
		path: 'chat',
		component: ConversationMainComponent,
		canActivate: [AuthGuard], // Protect this route
	},
	// Redirect root path or logged-in users to chat
	{ path: '', redirectTo: '/chat', pathMatch: 'full' },
];
