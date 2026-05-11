insert into tasks (id, project_id, name, status, created_at, updated_at)
select gen_random_uuid(), source.project_id, source.task_name, 'ACTIVE', now(), now()
from (
    select distinct on (entry.project_id, lower(trim(entry.description)))
        entry.project_id,
        trim(entry.description) as task_name
    from time_entries entry
    where entry.project_id is not null
      and entry.description is not null
      and trim(entry.description) <> ''
    order by entry.project_id, lower(trim(entry.description)), entry.started_at desc
) source
where not exists (
    select 1
    from tasks existing
    where existing.project_id = source.project_id
      and lower(existing.name) = lower(source.task_name)
);

update time_entries entry
set task_id = task.id,
    description = null
from tasks task
where entry.project_id = task.project_id
  and entry.description is not null
  and trim(entry.description) <> ''
  and lower(task.name) = lower(trim(entry.description));

update time_entries
set description = null
where description is not null
  and trim(description) <> '';
