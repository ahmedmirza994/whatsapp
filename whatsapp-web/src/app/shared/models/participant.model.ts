export interface Participant {
	id: string;
	userId: string;
	name: string;
	profilePicture: string | null;
	joinedAt: string;
	initial: string | null;
	leftAt: string | null;
	lastReadAt: string | null;
}
