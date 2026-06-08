insert into user_preferences (user_id, updated_at)
select users.id, now()
from users
where not exists (
    select 1
    from user_preferences
    where user_preferences.user_id = users.id
);

delete from notification_push_subscriptions older
using notification_push_subscriptions newer
where older.endpoint = newer.endpoint
  and older.id <> newer.id
  and (
      older.updated_at < newer.updated_at
      or (
          older.updated_at = newer.updated_at
          and older.created_at < newer.created_at
      )
      or (
          older.updated_at = newer.updated_at
          and older.created_at = newer.created_at
          and older.id::text < newer.id::text
      )
  );

alter table notification_push_subscriptions
    drop constraint if exists notification_push_subscriptions_user_id_endpoint_key;

alter table notification_push_subscriptions
    add constraint uk_notification_push_subscriptions_endpoint unique (endpoint);

update user_preferences
set browser_push_enabled = exists (
        select 1
        from notification_push_subscriptions
        where notification_push_subscriptions.user_id = user_preferences.user_id
    ),
    updated_at = now()
where browser_push_enabled = true;
