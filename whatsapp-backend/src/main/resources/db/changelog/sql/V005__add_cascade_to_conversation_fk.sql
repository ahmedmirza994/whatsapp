alter table conversation_participants
drop constraint if exists conversation_participants_conversation_id_fkey;

alter table conversation_participants
	add constraint conversation_participants_conversation_id_fkey
		foreign key (conversation_id)
			references conversations(id)
			on delete cascade;

alter table messages
drop constraint if exists messages_conversation_id_fkey;

alter table messages
	add constraint messages_conversation_id_fkey
		foreign key (conversation_id)
			references conversations(id)
			on delete cascade;
