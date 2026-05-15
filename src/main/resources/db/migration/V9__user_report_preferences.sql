alter table user_preferences
    add column include_organization_entries_in_personal_reports boolean not null default true;
