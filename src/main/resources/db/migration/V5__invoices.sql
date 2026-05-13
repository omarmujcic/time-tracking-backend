alter table organizations
    add column billing_name varchar(160),
    add column billing_contact_person varchar(160),
    add column billing_address_line_1 varchar(220),
    add column billing_address_line_2 varchar(220),
    add column billing_postal_code varchar(40),
    add column billing_city varchar(120),
    add column billing_country varchar(120),
    add column billing_email varchar(254),
    add column billing_phone varchar(40),
    add column billing_tax_id varchar(80),
    add column billing_registration_number varchar(80);

create table invoice_settings (
    id uuid primary key,
    workspace_type varchar(20) not null,
    user_id uuid references users(id) on delete cascade,
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
    to_name varchar(160),
    to_contact_person varchar(160),
    to_address_line_1 varchar(220),
    to_address_line_2 varchar(220),
    to_postal_code varchar(40),
    to_city varchar(120),
    to_country varchar(120),
    to_email varchar(254),
    to_phone varchar(40),
    to_tax_id varchar(80),
    to_registration_number varchar(80),
    next_invoice_number integer not null default 1,
    tax_label varchar(80) not null default 'Tax',
    tax_rate numeric(5, 2) not null default 0.00,
    terms text,
    due_days integer not null default 14,
    currency varchar(3) not null default 'EUR',
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_invoice_settings_workspace check (
        (workspace_type = 'PERSONAL' and user_id is not null and organization_id is null)
        or (workspace_type = 'ORGANIZATION' and user_id is null and organization_id is not null)
    ),
    constraint chk_invoice_settings_next_number check (next_invoice_number > 0),
    constraint chk_invoice_settings_due_days check (due_days >= 0),
    constraint chk_invoice_settings_tax_rate check (tax_rate >= 0)
);

create unique index uk_invoice_settings_personal on invoice_settings(user_id) where user_id is not null;
create unique index uk_invoice_settings_organization on invoice_settings(organization_id) where organization_id is not null;

create table invoices (
    id uuid primary key,
    workspace_type varchar(20) not null,
    user_id uuid references users(id) on delete cascade,
    organization_id uuid references organizations(id) on delete cascade,
    created_by_user_id uuid not null references users(id),
    invoice_number varchar(80) not null,
    issue_date date not null,
    due_date date not null,
    period_start date not null,
    period_end date not null,
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
    to_name varchar(160),
    to_contact_person varchar(160),
    to_address_line_1 varchar(220),
    to_address_line_2 varchar(220),
    to_postal_code varchar(40),
    to_city varchar(120),
    to_country varchar(120),
    to_email varchar(254),
    to_phone varchar(40),
    to_tax_id varchar(80),
    to_registration_number varchar(80),
    subtotal numeric(12, 2) not null,
    tax_label varchar(80) not null,
    tax_rate numeric(5, 2) not null,
    tax_amount numeric(12, 2) not null,
    total numeric(12, 2) not null,
    currency varchar(3) not null default 'EUR',
    terms text,
    created_at timestamptz not null,
    constraint chk_invoices_workspace check (
        (workspace_type = 'PERSONAL' and user_id is not null and organization_id is null)
        or (workspace_type = 'ORGANIZATION' and organization_id is not null)
    )
);

create index idx_invoices_personal_created_at on invoices(user_id, created_at desc) where user_id is not null;
create index idx_invoices_organization_created_at on invoices(organization_id, created_at desc) where organization_id is not null;

create table invoice_lines (
    id uuid primary key,
    invoice_id uuid not null references invoices(id) on delete cascade,
    line_order integer not null,
    project_key varchar(180) not null,
    project_id uuid references projects(id) on delete set null,
    project_name varchar(160) not null,
    description varchar(220),
    duration_seconds bigint not null,
    quantity numeric(10, 2) not null default 1.00,
    unit_price numeric(12, 2) not null,
    tax_rate numeric(5, 2) not null,
    total_amount numeric(12, 2) not null
);

create index idx_invoice_lines_invoice_order on invoice_lines(invoice_id, line_order);
