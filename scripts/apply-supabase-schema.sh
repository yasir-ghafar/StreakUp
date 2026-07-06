#!/usr/bin/env bash
# Applies StreakUp schema to your Supabase Postgres database.
# Requires SUPABASE_DB_PASSWORD in local.properties (from Dashboard → Settings → Database).

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PROPS="$ROOT/local.properties"
MIGRATIONS_DIR="$ROOT/supabase/migrations"
PROJECT_REF="fwictjlsdhlzrpqhycfs"

if [[ ! -f "$PROPS" ]]; then
  echo "Error: local.properties not found"
  exit 1
fi

get_prop() {
  local val
  val="$(grep "^$1=" "$PROPS" 2>/dev/null | cut -d'=' -f2- || true)"
  # Strip optional surrounding quotes
  val="${val%\"}"
  val="${val#\"}"
  val="${val%\'}"
  val="${val#\'}"
  echo "$val"
}

DB_PASSWORD="$(get_prop SUPABASE_DB_PASSWORD)"

if [[ -z "$DB_PASSWORD" ]]; then
  echo "SUPABASE_DB_PASSWORD not set in local.properties."
  echo ""
  echo "Add your database password:"
  echo "  SUPABASE_DB_PASSWORD=your-db-password"
  echo ""
  echo "Find it in Supabase Dashboard → Project Settings → Database → Database password"
  echo ""
  echo "Or paste SQL manually from: supabase/migrations/"
  echo "  Dashboard → SQL Editor → New query → Run"
  exit 1
fi

if ! command -v psql &>/dev/null; then
  echo "psql not found. Install PostgreSQL client or run SQL manually in Supabase Dashboard."
  exit 1
fi

DB_HOST="db.${PROJECT_REF}.supabase.co"

echo "Applying migrations to Supabase project $PROJECT_REF ..."
for sql in $(ls "$MIGRATIONS_DIR"/*.sql | sort); do
  echo "  → $(basename "$sql")"
  PGPASSWORD="$DB_PASSWORD" psql \
    -h "$DB_HOST" \
    -p 5432 \
    -U postgres \
    -d postgres \
    -f "$sql" \
    -v ON_ERROR_STOP=1
done

echo "Verifying profiles table..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "apikey: $(get_prop SUPABASE_ANON_KEY)" \
  "https://${PROJECT_REF}.supabase.co/rest/v1/profiles?select=id,name,gender&limit=1")

if [[ "$HTTP_CODE" == "200" ]]; then
  echo "Success! Supabase schema is ready."
else
  echo "Schema applied but REST check returned HTTP $HTTP_CODE (may need a moment to propagate)."
fi
