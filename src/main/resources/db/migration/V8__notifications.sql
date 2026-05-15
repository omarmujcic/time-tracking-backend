create table notifications (
    id uuid primary key,
    workspace_type varchar(20) not null,
    workspace_user_id uuid references users(id) on delete cascade,
    organization_id uuid references organizations(id) on delete cascade,
    created_by_user_id uuid not null references users(id) on delete cascade,
    type varchar(60) not null,
    status varchar(20) not null,
    message text not null,
    subject_type varchar(60),
    subject_id uuid,
    subject_label varchar(220),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    resolved_at timestamptz,
    resolved_by_user_id uuid references users(id),
    constraint chk_notifications_workspace check (
        (workspace_type = 'PERSONAL' and workspace_user_id is not null and organization_id is null)
        or (workspace_type = 'ORGANIZATION' and workspace_user_id is null and organization_id is not null)
    ),
    constraint chk_notifications_type check (
        type in ('PROJECT_BILLING_ISSUE')
    ),
    constraint chk_notifications_status check (
        status in ('OPEN', 'RESOLVED')
    )
);

create index idx_notifications_org_status_created
    on notifications(organization_id, status, created_at desc);

create index idx_notifications_creator
    on notifications(created_by_user_id, created_at desc);

create table notification_dismissals (
    id uuid primary key,
    notification_id uuid not null references notifications(id) on delete cascade,
    user_id uuid not null references users(id) on delete cascade,
    dismissed_at timestamptz not null,
    unique (notification_id, user_id)
);

create index idx_notification_dismissals_user
    on notification_dismissals(user_id, notification_id);
