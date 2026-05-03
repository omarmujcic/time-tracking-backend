create table users (
    id uuid primary key,
    username varchar(120) not null unique,
    display_name varchar(160) not null,
    password_hash varchar(255) not null,
    created_at timestamptz not null
);
