package com.techlad.streakup.data.remote

import io.github.jan.supabase.SupabaseClient

/** Wrapper so Koin can hold a nullable Supabase client as a non-null singleton. */
class SupabaseProvider(val client: SupabaseClient?)
