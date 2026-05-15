create table project_billing_rules (
    id uuid primary key,
    project_id uuid not null references projects(id) on delete cascade,
    type varchar(40) not null,
    effective_from date not null,
    monthly_amount numeric(12, 2),
    base_amount numeric(12, 2),
    included_hours numeric(10, 2),
    overage_hourly_rate numeric(10, 2),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    unique (project_id, effective_from),
    constraint chk_project_billing_rule_type check (
        type in ('HOURLY', 'FIXED_MONTHLY', 'MONTHLY_BASE_PLUS_OVERAGE')
    ),
    constraint chk_project_billing_rule_amounts check (
        (type = 'HOURLY'
            and monthly_amount is null
            and base_amount is null
            and included_hours is null
            and overage_hourly_rate is null)
        or (type = 'FIXED_MONTHLY'
            and monthly_amount is not null
            and monthly_amount >= 0
            and base_amount is null
            and included_hours is null
            and overage_hourly_rate is null)
        or (type = 'MONTHLY_BASE_PLUS_OVERAGE'
            and monthly_amount is null
            and base_amount is not null
            and base_amount >= 0
            and included_hours is not null
            and included_hours >= 0
            and overage_hourly_rate is not null
            and overage_hourly_rate >= 0)
    )
);

create index idx_project_billing_rules_project_effective
    on project_billing_rules(project_id, effective_from desc);

insert into project_billing_rules (
    id,
    project_id,
    type,
    effective_from,
    created_at,
    updated_at
)
select gen_random_uuid(), id, 'HOURLY', date '1970-01-01', now(), now()
from projects;
