#!/usr/bin/env bash
# Step 1: export the legacy SQLite views/downloads tables to CSV.
# Run against a COPY of metrics.db (not the live file on the CIFS share).
#   ./01_export_sqlite.sh /path/to/metrics-copy.db ./export
set -euo pipefail
DB="${1:?sqlite db file}"
OUT="${2:-./export}"
mkdir -p "$OUT"

sqlite3 "$DB" "PRAGMA integrity_check;" | grep -qx ok || { echo "integrity_check failed" >&2; exit 1; }

for t in views downloads; do
  sqlite3 -header -csv "$DB" \
    "SELECT start_timestamp, end_timestamp, amount, document, doc_title, record_type FROM $t;" \
    > "$OUT/$t.csv"
  echo "$t: $(sqlite3 "$DB" "SELECT count(*) || ' rows, sum(amount)=' || coalesce(sum(amount),0) FROM $t;")"
done
