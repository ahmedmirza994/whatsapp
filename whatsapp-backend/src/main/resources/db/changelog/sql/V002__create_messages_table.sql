-- create conversations table
create table conversations (
    id uuid primary key default gen_random_uuid(),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

-- create conversation participants table
create table conversation_participants (
    id uuid primary key default gen_random_uuid(),
    conversation_id uuid not null references conversations(id) on delete cascade,
    user_id uuid not null references users(id) on delete cascade,
    joined_at timestamp not null default current_timestamp,
    constraint uk_conversation_participant unique (conversation_id, user_id)
);

-- create messages table
create table messages (
    id uuid primary key default gen_random_uuid(),
    conversation_id uuid not null references conversations(id) on delete cascade,
    sender_id uuid not null references users(id) on delete cascade,
    content text not null,
    sent_at timestamp not null default current_timestamp
);

-- create index on conversation_id for faster message retrieval
create index idx_message_conversation on messages (conversation_id);
create index idx_message_sender on messages (sender_id);
create index idx_conversation_user on conversation_participants (user_id);

create or replace function update_conversation_timestamp()
returns trigger as $BODY$
begin
    update conversations set updated_at = current_timestamp where id = new.conversation_id;
    return new;
end;
$BODY$ language plpgsql;

create trigger trg_update_conversation
after insert on messages
for each row
execute function update_conversation_timestamp();
