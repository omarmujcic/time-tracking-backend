create table invoice_user_settings (
    id uuid primary key,
    workspace_type varchar(20) not null,
    user_id uuid not null references users(id) on delete cascade,
    organization_id uuid references organizations(id) on delete cascade,
    from_name varchar(160),
    from_contact_person varchar(160),
    from_address_line_1 varchar(220),
    from_address_line_2 varchar(220),
    from_postal_code varchar(40),
    from_city varchar(120),
    from_country varchar(120),
    from_email varchar(254),
    from_phone varchar(40),
    from_tax_id varchar(80),
    from_registration_number varchar(80),
    next_invoice_number integer not null default 1,
    tax_label varchar(80) not null default 'Tax',
    tax_rate numeric(5, 2) not null default 0.00,
    terms text,
    due_days integer not null default 14,
    currency varchar(3) not null default 'EUR',
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_invoice_user_settings_workspace check (
        (workspace_type = 'PERSONAL' and organization_id is null)
        or (workspace_type = 'ORGANIZATION' and organization_id is not null)
    ),
    constraint chk_invoice_user_settings_next_number check (next_invoice_number > 0),
    constraint chk_invoice_user_settings_due_days check (due_days >= 0),
    constraint chk_invoice_user_settings_tax_rate check (tax_rate >= 0)
);

create unique index uk_invoice_user_settings_personal
    on invoice_user_settings(user_id)
    where workspace_type = 'PERSONAL' and organization_id is null;

create unique index uk_invoice_user_settings_organization
    on invoice_user_settings(user_id, organization_id)
    where workspace_type = 'ORGANIZATION' and organization_id is not null;

insert into invoice_user_settings (
    id,
    workspace_type,
    user_id,
    organization_id,
    from_name,
    from_contact_person,
    from_address_line_1,
    from_address_line_2,
    from_postal_code,
    from_city,
    from_country,
    from_email,
    from_phone,
    from_tax_id,
    from_registration_number,
    next_invoice_number,
    tax_label,
    tax_rate,
    terms,
    due_days,
    currency,
    created_at,
    updated_at
)
select
    gen_random_uuid(),
    'PERSONAL',
    user_id,
    null,
    from_name,
    from_contact_person,
    from_address_line_1,
    from_address_line_2,
    from_postal_code,
    from_city,
    from_country,
    from_email,
    from_phone,
    from_tax_id,
    from_registration_number,
    next_invoice_number,
    tax_label,
    tax_rate,
    terms,
    due_days,
    currency,
    created_at,
    updated_at
from invoice_settings
where workspace_type = 'PERSONAL'
  and user_id is not null
on conflict do nothing;
