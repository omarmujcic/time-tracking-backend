alter table organizations
    add column members_can_create_tasks boolean not null default true;
