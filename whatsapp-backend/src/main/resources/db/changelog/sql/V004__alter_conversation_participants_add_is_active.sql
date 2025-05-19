alter table conversation_participants
  add column is_active boolean default true,
  add column left_at timestamp with time zone;
