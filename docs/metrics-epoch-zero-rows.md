# Issue: metrics rows with `start_timestamp = 0`

## What they are

Until the `lastRun` fix, `JDBCMetricsService.lastRun` was only assigned at the end of a sync, so it
held the field default `0` until the first sync after every application start. Each of those first
syncs wrote one row per document viewed or downloaded in the hour after boot with
`start_timestamp = 0` (1 January 1970).

## How to apply (`the app repairs it on startup`)
**Back up**
1. Monitoring **after** an `Exporting metric counts` log line, so no write is under way,
2. Copy the database file `metrics.db` to `metrics.db.bak-$(date +%Y%m%d)`
3. Run the following SQL to check the rows, it is used to verify the patch:
```verifying sql
SELECT 'views' AS tbl, count(*) AS epoch0_rows, count(DISTINCT end_timestamp / 3600) AS restart_batches,
    coalesce(sum(amount), 0) AS epoch0_amount, min(end_timestamp) AS first_end, max(end_timestamp) AS last_end
    FROM views WHERE start_timestamp = 0
UNION ALL
SELECT 'downloads', count(*), count(DISTINCT end_timestamp / 3600),
    coalesce(sum(amount), 0), min(end_timestamp), max(end_timestamp)
    FROM downloads WHERE start_timestamp = 0;

SELECT 'views' AS tbl, count(*) AS all_rows, coalesce(sum(amount), 0) AS all_amount FROM views
UNION ALL
SELECT 'downloads', count(*), coalesce(sum(amount), 0) FROM downloads;
```
**Apply the patch**
1. Deploy Catalogue with env varible METRICS_REPAIR_EPOCH0_ENABLED: `true`, Catalogue will run below sql to patch the rows.
```patching sql
UPDATE views     SET start_timestamp = (end_timestamp - 3600) WHERE start_timestamp = 0;
UPDATE downloads SET start_timestamp = (end_timestamp - 3600) WHERE start_timestamp = 0;
```
2. Run the same sql as in **Back up** to verify the patch, epoch0_rows should be 0.

**Disable the patch**
1. Deploy Catalogue with env varible METRICS_REPAIR_EPOCH0_ENABLED: `false`.
2. If the patch was successful, the code for this change can be removed afterward.
