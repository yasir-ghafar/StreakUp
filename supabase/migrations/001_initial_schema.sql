-- StreakUp initial schema
-- Run via: ./scripts/apply-supabase-schema.sh
-- Or paste into Supabase Dashboard → SQL Editor

-- ---------------------------------------------------------------------------
-- Profiles (extends auth.users)
-- ---------------------------------------------------------------------------
create table if not exists public.profiles (
    id uuid primary key references auth.users (id) on delete cascade,
    email text,
    display_name text,
    name text,
    gender text,
    password_updated_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

alter table public.profiles enable row level security;

drop policy if exists "Users can view own profile" on public.profiles;
create policy "Users can view own profile"
    on public.profiles for select
    using (auth.uid() = id);

drop policy if exists "Users can insert own profile" on public.profiles;
create policy "Users can insert own profile"
    on public.profiles for insert
    with check (auth.uid() = id);

drop policy if exists "Users can update own profile" on public.profiles;
create policy "Users can update own profile"
    on public.profiles for update
    using (auth.uid() = id);

-- ---------------------------------------------------------------------------
-- User settings (must exist before auth trigger)
-- ---------------------------------------------------------------------------
create table if not exists public.user_settings (
    user_id uuid primary key references auth.users (id) on delete cascade,
    notifications_enabled boolean not null default true,
    daily_summary_enabled boolean not null default false,
    daily_summary_time time not null default '20:00',
    theme text not null default 'system' check (theme in ('light', 'dark', 'system')),
    is_guest boolean not null default false,
    updated_at timestamptz not null default now()
);

alter table public.user_settings enable row level security;

drop policy if exists "Users can view own settings" on public.user_settings;
create policy "Users can view own settings"
    on public.user_settings for select
    using (auth.uid() = user_id);

drop policy if exists "Users can insert own settings" on public.user_settings;
create policy "Users can insert own settings"
    on public.user_settings for insert
    with check (auth.uid() = user_id);

drop policy if exists "Users can update own settings" on public.user_settings;
create policy "Users can update own settings"
    on public.user_settings for update
    using (auth.uid() = user_id);

-- Auto-create profile + settings on sign-up
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

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- ---------------------------------------------------------------------------
-- Habits
-- ---------------------------------------------------------------------------
create table if not exists public.habits (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users (id) on delete cascade,
    name text not null,
    icon text not null default '✅',
    color text not null default '#5DCAA5',
    frequency_type text not null default 'daily'
        check (frequency_type in ('daily', 'weekly')),
    frequency_target integer not null default 1
        check (frequency_target >= 1 and frequency_target <= 7),
    sort_order integer not null default 0,
    reminder_time time,
    is_archived boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists habits_user_id_idx on public.habits (user_id);
create index if not exists habits_user_active_idx on public.habits (user_id, is_archived, sort_order);

alter table public.habits enable row level security;

drop policy if exists "Users can view own habits" on public.habits;
create policy "Users can view own habits"
    on public.habits for select
    using (auth.uid() = user_id);

drop policy if exists "Users can insert own habits" on public.habits;
create policy "Users can insert own habits"
    on public.habits for insert
    with check (auth.uid() = user_id);

drop policy if exists "Users can update own habits" on public.habits;
create policy "Users can update own habits"
    on public.habits for update
    using (auth.uid() = user_id);

drop policy if exists "Users can delete own habits" on public.habits;
create policy "Users can delete own habits"
    on public.habits for delete
    using (auth.uid() = user_id);

-- ---------------------------------------------------------------------------
-- Check-ins (one per habit per day)
-- ---------------------------------------------------------------------------
create table if not exists public.check_ins (
    id uuid primary key default gen_random_uuid(),
    habit_id uuid not null references public.habits (id) on delete cascade,
    user_id uuid not null references auth.users (id) on delete cascade,
    checked_date date not null,
    created_at timestamptz not null default now(),
    unique (habit_id, checked_date)
);

create index if not exists check_ins_habit_date_idx on public.check_ins (habit_id, checked_date desc);
create index if not exists check_ins_user_date_idx on public.check_ins (user_id, checked_date desc);

alter table public.check_ins enable row level security;

drop policy if exists "Users can view own check-ins" on public.check_ins;
create policy "Users can view own check-ins"
    on public.check_ins for select
    using (auth.uid() = user_id);

drop policy if exists "Users can insert own check-ins" on public.check_ins;
create policy "Users can insert own check-ins"
    on public.check_ins for insert
    with check (auth.uid() = user_id);

drop policy if exists "Users can delete own check-ins" on public.check_ins;
create policy "Users can delete own check-ins"
    on public.check_ins for delete
    using (auth.uid() = user_id);

-- ---------------------------------------------------------------------------
-- Updated-at trigger
-- ---------------------------------------------------------------------------
create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
    new.updated_at = now();
    return new;
end;
$$;

drop trigger if exists habits_updated_at on public.habits;
create trigger habits_updated_at
    before update on public.habits
    for each row execute function public.set_updated_at();

drop trigger if exists profiles_updated_at on public.profiles;
create trigger profiles_updated_at
    before update on public.profiles
    for each row execute function public.set_updated_at();

drop trigger if exists user_settings_updated_at on public.user_settings;
create trigger user_settings_updated_at
    before update on public.user_settings
    for each row execute function public.set_updated_at();

-- ---------------------------------------------------------------------------
-- Streak helper view
-- ---------------------------------------------------------------------------
create or replace view public.habit_streak_stats as
select
    h.id as habit_id,
    h.user_id,
    h.name,
    count(c.id) as total_check_ins,
    max(c.checked_date) as last_check_in_date
from public.habits h
left join public.check_ins c on c.habit_id = h.id
where h.is_archived = false
group by h.id, h.user_id, h.name;
