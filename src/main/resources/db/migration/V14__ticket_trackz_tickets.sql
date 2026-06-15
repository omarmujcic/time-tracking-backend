create sequence ticket_trackz_ticket_key_seq start with 1 increment by 1;

create table ticket_trackz_tickets (
    id uuid primary key,
    ticket_key varchar(32) not null unique,
    workspace_type varchar(20) not null,
    user_id uuid references users(id) on delete cascade,
    organization_id uuid references organizations(id) on delete cascade,
    project_id uuid not null references projects(id) on delete cascade,
    assignee_user_id uuid references users(id) on delete set null,
    created_by_user_id uuid not null references users(id),
    title varchar(160) not null,
    description text,
    status varchar(24) not null,
    priority varchar(8),
    due_date date,
    story_points integer,
    estimated_hours numeric(8, 2),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_ticket_trackz_tickets_workspace check (
        (workspace_type = 'PERSONAL' and user_id is not null and organization_id is null)
        or (workspace_type = 'ORGANIZATION' and user_id is null and organization_id is not null)
    ),
    constraint chk_ticket_trackz_story_points check (story_points is null or story_points >= 0),
    constraint chk_ticket_trackz_estimated_hours check (estimated_hours is null or estimated_hours >= 0)
);

create index idx_ticket_trackz_workspace_created_at
    on ticket_trackz_tickets(workspace_type, user_id, organization_id, created_at desc);

create index idx_ticket_trackz_assignee_status
    on ticket_trackz_tickets(assignee_user_id, status);

create index idx_ticket_trackz_project_status
    on ticket_trackz_tickets(project_id, status);
