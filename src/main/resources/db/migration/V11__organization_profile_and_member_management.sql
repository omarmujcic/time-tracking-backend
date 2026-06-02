alter table organizations
    add column legal_name varchar(160),
    add column business_address_line_1 varchar(220),
    add column business_address_line_2 varchar(220),
    add column business_postal_code varchar(40),
    add column business_city varchar(120),
    add column business_country varchar(120),
    add column timezone varchar(80) not null default 'UTC',
    add column default_currency varchar(3) not null default 'EUR';
