export interface User {
	id: string;
	name: string;
	email: string;
	phone: string | null;
	profilePictureUrl: string | null;
	jwtToken: string;
}
