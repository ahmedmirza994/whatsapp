create table users
(
	id                  uuid primary key      default gen_random_uuid(),
	name                varchar(255) not null,
	email               varchar(255) not null unique,
	password            varchar(255) not null,
	phone               varchar(15),
	profile_picture_url varchar(512),
	created_at          timestamp    not null default current_timestamp,
	updated_at          timestamp
);

create index idx_users_email on users (email);
