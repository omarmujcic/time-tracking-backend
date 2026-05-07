create table time_entries (
    id uuid primary key,
    user_id uuid not null references users(id),
    project_name varchar(160) not null,
    description varchar(500),
    hourly_rate numeric(10, 2) not null,
    currency varchar(3) not null default 'EUR',
    started_at timestamptz not null,
    ended_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_time_entries_user_started_at on time_entries(user_id, started_at desc);
create index idx_time_entries_user_active on time_entries(user_id) where ended_at is null;
