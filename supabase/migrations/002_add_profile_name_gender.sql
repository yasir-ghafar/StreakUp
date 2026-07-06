-- Add name and gender to profiles (existing projects)

alter table public.profiles add column if not exists name text;
alter table public.profiles add column if not exists gender text;

update public.profiles
set name = display_name
where name is null and display_name is not null;

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
    insert into public.profiles (id, email, display_name, name, gender)
    values (
        new.id,
        new.email,
        coalesce(
            new.raw_user_meta_data ->> 'name',
            new.raw_user_meta_data ->> 'display_name',
            split_part(new.email, '@', 1)
        ),
        coalesce(
            new.raw_user_meta_data ->> 'name',
            new.raw_user_meta_data ->> 'display_name',
            split_part(new.email, '@', 1)
        ),
        new.raw_user_meta_data ->> 'gender'
    )
    on conflict (id) do nothing;

    insert into public.user_settings (user_id)
    values (new.id)
    on conflict (user_id) do nothing;

    return new;
end;
$$;
