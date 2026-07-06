# StreakUp

A habit tracking app with streaks, heatmaps, and Supabase sync.

## Features

- **Habit management** — create, edit, archive, reorder habits with emoji icons and colors
- **One-tap check-off** — tap to mark/unmark today's completion
- **Streak tracking** — current and longest streaks (daily and weekly frequency)
- **Calendar heatmap** — GitHub-style activity view per habit
- **Stats** — weekly and monthly completion charts
- **Offline-first** — Room local cache, syncs to Supabase when online
- **Auth** — email/password sign-in or guest mode (local only)
- **Settings** — notifications, theme (light/dark/system)

## Setup

### 1. Supabase Project

1. Create a project at [supabase.com](https://supabase.com)
2. Enable **Email** auth under Authentication → Providers
3. Add credentials to `local.properties`:

```properties
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=your-anon-key
SUPABASE_DB_PASSWORD=your-database-password
```

4. Apply the database schema (choose one):

**Option A — Script (recommended):**
```bash
./scripts/apply-supabase-schema.sh
```

**Option B — SQL Editor:**  
Open Supabase Dashboard → SQL Editor, paste and run `supabase/migrations/001_initial_schema.sql`

Open the project in Android Studio and run on a device or emulator (API 24+).

### Guest → Cloud sync

Users in **guest mode** can go to **Settings → Sign in & sync to cloud** to create an account or sign in. All local habits and check-ins are uploaded to Supabase automatically.

## Database Schema

| Table | Purpose |
|-------|---------|
| `profiles` | User profile (auto-created on sign-up) |
| `user_settings` | Notification prefs, theme, guest flag |
| `habits` | Habit definitions with frequency and reminders |
| `check_ins` | One row per habit per day |

All tables use Row Level Security — users can only access their own data.

## Architecture

```
UI (Compose) → ViewModel → Repository → Room (local) + Supabase (remote)
```

- **MVVM** with Kotlin Flow
- **Koin** for dependency injection
- **Room** for offline-first storage
- **supabase-kt** for auth and sync

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| Splash | `/splash` | Auth gate, sync on launch |
| Login | `/login` | Email/password + guest mode |
| Home | `/home` | Today's habits, progress ring, FAB |
| Add/Edit Habit | `/add_habit`, `/edit_habit/{id}` | Form with icon, color, frequency |
| Habit Detail | `/habit_detail/{id}` | Heatmap, streaks, archive/delete |
| Stats | `/stats` | Weekly/monthly completion charts |
| Settings | `/settings` | Notifications, theme, sign out |

## Coming Soon

- Home screen widget (tappable habit circles)
- Local reminder notifications
- Streak freeze
- Google Sign-In
- Drag-to-reorder on home screen
