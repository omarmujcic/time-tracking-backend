create extension if not exists pgcrypto;

alter table users
    add column email varchar(254),
    add column phone varchar(40),
    add column active_workspace_type varchar(20) not null default 'PERSONAL',
    add column active_organization_id uuid;

create unique index uk_users_email_lower on users(lower(email)) where email is not null;

create table user_preferences (
    user_id uuid primary key references users(id) on delete cascade,
    language varchar(12) not null default 'en',
    theme_mode varchar(20) not null default 'SYSTEM',
    grouped_entries_enabled boolean not null default true,
    date_format varchar(32) not null default 'YYYY-MM-DD',
    decimal_separator varchar(8) not null default 'DOT',
    timezone varchar(80) not null default 'UTC',
    updated_at timestamptz not null
);

insert into user_preferences (user_id, updated_at)
select id, now()
from users;

create table organizations (
    id uuid primary key,
    name varchar(160) not null,
    join_code varchar(24) not null unique,
    created_by_user_id uuid not null references users(id),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table organization_members (
    id uuid primary key,
    organization_id uuid not null references organizations(id) on delete cascade,
    user_id uuid not null references users(id) on delete cascade,
    role varchar(20) not null,
    joined_at timestamptz not null,
    unique (organization_id, user_id)
);

alter table users
    add constraint fk_users_active_organization foreign key (active_organization_id) references organizations(id);

create table projects (
    id uuid primary key,
    user_id uuid references users(id) on delete cascade,
    organization_id uuid references organizations(id) on delete cascade,
    name varchar(160) not null,
    status varchar(20) not null default 'ACTIVE',
    hourly_rate numeric(10, 2) not null default 0.01,
    currency varchar(3) not null default 'EUR',
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_projects_workspace check (
        (user_id is not null and organization_id is null)
        or (user_id is null and organization_id is not null)
    )
);

create unique index uk_projects_personal_name on projects(user_id, lower(name)) where user_id is not null;
create unique index uk_projects_organization_name on projects(organization_id, lower(name)) where organization_id is not null;

create table tasks (
    id uuid primary key,
    project_id uuid not null references projects(id) on delete cascade,
    name varchar(160) not null,
    status varchar(20) not null default 'ACTIVE',
    created_at timestamptz not null,
    updated_at timestamptz not null,
    unique (project_id, name)
);

alter table time_entries
    add column workspace_type varchar(20),
    add column organization_id uuid references organizations(id),
    add column project_id uuid references projects(id),
    add column task_id uuid references tasks(id);

insert into projects (id, user_id, name, status, hourly_rate, currency, created_at, updated_at)
select gen_random_uuid(), user_id, project_name, 'ACTIVE', max(hourly_rate), max(currency), now(), now()
from (
    select distinct on (user_id, lower(trim(project_name)))
        user_id,
        trim(project_name) as project_name,
        hourly_rate,
        currency
    from time_entries
    where project_name is not null and trim(project_name) <> ''
    order by user_id, lower(trim(project_name)), started_at desc
) distinct_projects
group by user_id, project_name;

update time_entries entry
set workspace_type = 'PERSONAL',
    project_id = project.id
from projects project
where project.user_id = entry.user_id
  and lower(project.name) = lower(trim(entry.project_name))
  and project.organization_id is null;

alter table time_entries
    alter column workspace_type set default 'PERSONAL';

create index idx_time_entries_workspace_started_at on time_entries(workspace_type, organization_id, user_id, started_at desc);
create index idx_time_entries_project_id on time_entries(project_id);
create index idx_tasks_project_status on tasks(project_id, status);
