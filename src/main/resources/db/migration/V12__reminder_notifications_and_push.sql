alter table user_preferences
    add column long_timer_reminders_enabled boolean not null default true,
    add column missing_daily_time_reminders_enabled boolean not null default true,
    add column invoice_reminders_enabled boolean not null default true,
    add column browser_push_enabled boolean not null default false;

alter table notifications
    drop constraint chk_notifications_type;

alter table notifications
    add column recipient_user_id uuid references users(id) on delete cascade,
    add column source_route varchar(260),
    add column source_label varchar(160),
    add column reminder_key varchar(220),
    add column email_escalation_due_at timestamptz,
    add column email_escalated_at timestamptz;

alter table notifications
    add constraint chk_notifications_type check (
        type in (
            'PROJECT_BILLING_ISSUE',
            'LONG_RUNNING_TIMER',
            'MISSING_DAILY_TIME',
            'INVOICE_PERIOD_REMINDER'
        )
    );

create unique index uk_notifications_reminder_key
    on notifications(reminder_key)
    where reminder_key is not null;

create index idx_notifications_personal_status_created
    on notifications(workspace_user_id, status, created_at desc)
    where workspace_user_id is not null;

create index idx_notifications_recipient_status_created
    on notifications(recipient_user_id, status, created_at desc)
    where recipient_user_id is not null;

create table notification_push_subscriptions (
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    endpoint text not null,
    p256dh_key text not null,
    auth_key text not null,
    user_agent varchar(500),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    last_failed_at timestamptz,
    unique (user_id, endpoint)
);

create index idx_notification_push_subscriptions_user
    on notification_push_subscriptions(user_id);
