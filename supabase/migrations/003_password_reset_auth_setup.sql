-- Password reset is handled by Supabase Auth (auth.users) — no auth table changes needed.
--
-- REQUIRED: Supabase Dashboard → Authentication → URL Configuration
--   Redirect URLs:  com.techlad.streakup://auth
--   Site URL:       com.techlad.streakup://auth
--
-- The Android app handles com.techlad.streakup://auth deep links for password recovery
-- and email confirmation. Users must open the reset link on the device with StreakUp installed.
--
-- Ensure the "Reset password" email template uses {{ .ConfirmationURL }}.

-- Optional profile metadata: when the user last changed their password.
alter table public.profiles add column if not exists password_updated_at timestamptz;
