export interface SignupRequest {
	name: string;
	email: string;
	password: string;
	phone: string | null;
}
