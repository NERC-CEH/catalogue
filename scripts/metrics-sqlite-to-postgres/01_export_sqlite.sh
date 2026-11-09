#!/usr/bin/env bash
# Step 1: export the legacy SQLite views/downloads tables to CSV.
# Run against a COPY of metrics.db (not the live file on the CIFS share).
#   ./01_export_sqlite.sh /path/to/metrics-copy.db ./export
# Optional: patch missing rows from an old database
#   ./01_export_sqlite.sh /path/to/metrics-copy.db ./export /path/to/metrics.old.db
set -euo pipefail
DB="${1:?sqlite db file}"
OUT="${2:-./export}"
OLD_DB="${3:-}"
mkdir -p "$OUT"

sqlite3 "$DB" "PRAGMA integrity_check;" | grep -qx ok || { echo "integrity_check failed" >&2; exit 1; }

# Patch missing rows from old database if provided
if [[ -n "$OLD_DB" ]]; then
  echo "Patching missing rows from $OLD_DB..."
  sqlite3 "$DB" "ATTACH '$OLD_DB' AS o;"
  for t in views downloads; do
    count=$(sqlite3 "$DB" "SELECT count(*) FROM (SELECT * FROM o.$t EXCEPT SELECT * FROM $t);")
    if [[ "$count" -gt 0 ]]; then
      echo "Patching $count missing rows in $t..."
      sqlite3 "$DB" "INSERT INTO $t SELECT * FROM o.$t EXCEPT SELECT * FROM $t;"
    fi
  done
  sqlite3 "$DB" "DETACH o;"
  echo "Patch complete."
fi

for t in views downloads; do
  sqlite3 -header -csv "$DB" \
    "SELECT start_timestamp, end_timestamp, amount, document, doc_title, record_type FROM $t;" \
    > "$OUT/$t.csv"
  echo "$t: $(sqlite3 "$DB" "SELECT count(*) || ' rows, sum(amount)=' || coalesce(sum(amount),0) FROM $t;")"
done
