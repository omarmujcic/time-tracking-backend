alter table ticket_trackz_tickets
    add column product_context text;

update ticket_trackz_tickets
set ticket_key = 'ID-' || regexp_replace(ticket_key, '^TT-0*([0-9]+)$', '\1')
where ticket_key ~ '^TT-[0-9]+$';

select setval(
    'ticket_trackz_ticket_key_seq',
    greatest(
        coalesce((
            select max((substring(ticket_key from '^ID-([0-9]+)$'))::bigint)
            from ticket_trackz_tickets
            where ticket_key ~ '^ID-[0-9]+$'
        ), 0) + 1,
        1
    ),
    false
);
