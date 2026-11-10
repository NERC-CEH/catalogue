-- Step 2: load the CSVs into staging and transform into metrics.documents / metrics.metric_counts.
-- Run with psql from the directory holding the CSVs:
--   psql "$CATALOGUE_DATASOURCE_URL_PSQL" -v ON_ERROR_STOP=1 -f 02_load_postgres.sql
-- The whole thing is one transaction: any failure leaves PostgreSQL untouched.

\set ON_ERROR_STOP on
BEGIN;

-- Staging, dropped automatically at commit
CREATE TEMP TABLE legacy_counts (
    start_timestamp bigint,
    end_timestamp   bigint,
    amount          integer,
    document        text,
    doc_title       text,
    record_type     text,
    metric_type     text
) ON COMMIT DROP;

CREATE TEMP TABLE legacy_raw (LIKE legacy_counts EXCLUDING ALL) ON COMMIT DROP;
ALTER TABLE legacy_raw DROP COLUMN metric_type;

\copy legacy_raw FROM 'views.csv' WITH (FORMAT csv, HEADER true)
INSERT INTO legacy_counts SELECT *, 'view' FROM legacy_raw;
TRUNCATE legacy_raw;
\copy legacy_raw FROM 'downloads.csv' WITH (FORMAT csv, HEADER true)
INSERT INTO legacy_counts SELECT *, 'download' FROM legacy_raw;

-- Guard against running twice: abort if any legacy row is already present.
-- (Not a time-window check: in a rolling deploy the new postgres pod starts counting before the old
-- sqlite pod's final shutdown flush, so their windows legitimately overlap by a few minutes.)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM legacy_counts l
        JOIN metrics.metric_counts m
          ON m.document_id::text = lower(l.document)
         AND m.metric_type   = l.metric_type
         AND m.counted_from  = to_timestamp(l.start_timestamp)
         AND m.counted_until = to_timestamp(l.end_timestamp)
         AND m.amount        = l.amount
    ) THEN
        RAISE EXCEPTION 'legacy rows already present in metrics.metric_counts - already migrated?';
    END IF;
END $$;

-- Report what will be skipped: the new schema keys on uuid, as JDBCMetricsService does
SELECT metric_type, count(*) AS rows_skipped, sum(amount) AS amount_skipped
FROM legacy_counts
WHERE document !~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'
GROUP BY metric_type;

DELETE FROM legacy_counts
WHERE document !~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

-- documents: one row per uuid, title/type from its most recent legacy row.
-- Existing rows (written by the postgres engine since cutover) keep their newer title/type.
INSERT INTO metrics.documents (document_id, doc_title, record_type)
SELECT DISTINCT ON (document::uuid)
       document::uuid,
       nullif(doc_title, ''),
       nullif(record_type, '')
FROM legacy_counts
ORDER BY document::uuid, end_timestamp DESC
ON CONFLICT (document_id) DO UPDATE SET
    doc_title   = coalesce(documents.doc_title, EXCLUDED.doc_title),
    record_type = coalesce(documents.record_type, EXCLUDED.record_type);

-- metric_counts: epoch seconds -> timestamptz (UTC)
INSERT INTO metrics.metric_counts (document_id, metric_type, counted_from, counted_until, amount)
SELECT document::uuid, metric_type, to_timestamp(start_timestamp), to_timestamp(end_timestamp), amount
FROM legacy_counts;

-- Verification: must match the totals printed by 01_export_sqlite.sh (minus skipped rows above)
SELECT metric_type, count(*) AS rows_in_staging, sum(amount) AS amount_in_staging
FROM legacy_counts GROUP BY metric_type;

SELECT metric_type, count(*) AS rows_migrated, sum(amount) AS amount_migrated
FROM metrics.metric_counts m
WHERE EXISTS (SELECT 1 FROM legacy_counts l
              WHERE lower(l.document) = m.document_id::text AND l.metric_type = m.metric_type
                AND to_timestamp(l.start_timestamp) = m.counted_from
                AND to_timestamp(l.end_timestamp) = m.counted_until)
GROUP BY metric_type;

COMMIT;

ANALYZE metrics.documents;
ANALYZE metrics.metric_counts;
