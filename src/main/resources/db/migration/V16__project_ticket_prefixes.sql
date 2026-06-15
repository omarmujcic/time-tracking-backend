alter table projects
    add column ticket_prefix varchar(12);

with raw_projects as (
    select
        id,
        user_id,
        organization_id,
        name,
        regexp_replace(upper(coalesce(name, '')), '[^A-Z0-9]', '', 'g') as raw_prefix
    from projects
),
base_projects as (
    select
        id,
        user_id,
        organization_id,
        name,
        case
            when length(raw_prefix) >= 3 then substring(raw_prefix from 1 for 3)
            when length(raw_prefix) = 2 then raw_prefix
            when length(raw_prefix) = 1 then raw_prefix || 'X'
            else 'PR'
        end as base_prefix
    from raw_projects
),
numbered_projects as (
    select
        id,
        base_prefix,
        row_number() over (
            partition by user_id, organization_id, base_prefix
            order by lower(name), id
        ) as duplicate_number
    from base_projects
),
final_prefixes as (
    select
        id,
        case
            when duplicate_number = 1 then base_prefix
            else left(base_prefix, greatest(2, 12 - length(duplicate_number::text))) || duplicate_number::text
        end as ticket_prefix
    from numbered_projects
)
update projects project
set ticket_prefix = final_prefixes.ticket_prefix
from final_prefixes
where project.id = final_prefixes.id;

alter table projects
    alter column ticket_prefix set not null,
    add constraint chk_projects_ticket_prefix check (ticket_prefix ~ '^[A-Z0-9]{2,12}$');

create unique index uk_projects_personal_ticket_prefix
    on projects(user_id, lower(ticket_prefix))
    where user_id is not null;

create unique index uk_projects_organization_ticket_prefix
    on projects(organization_id, lower(ticket_prefix))
    where organization_id is not null;

update ticket_trackz_tickets ticket
set ticket_key = project.ticket_prefix || '-' || substring(ticket.ticket_key from '^[A-Z0-9]+-([0-9]+)$')
from projects project
where ticket.project_id = project.id
  and ticket.ticket_key ~ '^[A-Z0-9]+-[0-9]+$';
