package uk.ac.ceh.gateway.catalogue.services;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.TimeConstants;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

@Profile("metrics")
@Slf4j
@Service
public class JDBCMetricsService implements MetricsService {
    @NonNull private final Map<String, Set<String>> viewed;
    @NonNull private final Map<String, Set<String>> downloaded;
    @NonNull private final SimpleJdbcInsert viewInserter;
    @NonNull private final SimpleJdbcInsert downloadInserter;
    @NonNull private final JdbcTemplate jdbcTemplate;
    @Nullable private long lastRun;

    // SQLite has no built-in datetime type, so we store dates as Unix timestamps (seconds since 1 Jan 1970)
    private static final String CREATE_STATEMENT = """
        CREATE TABLE IF NOT EXISTS %s (
            start_timestamp integer NOT NULL,
            end_timestamp integer NOT NULL,
            amount integer NOT NULL,
            document text NOT NULL
        )
        """;
    private static final String TOTAL_STATEMENT = "SELECT coalesce(sum(amount), 0) FROM %s WHERE document = ?";
    private static final String VIEW_TABLE = "views";
    private static final String DOWNLOAD_TABLE = "downloads";

    public JDBCMetricsService(@NonNull DataSource dataSource) {
        log.info("Creating {}", this);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.viewInserter = new SimpleJdbcInsert(dataSource).withTableName(VIEW_TABLE);
        this.downloadInserter = new SimpleJdbcInsert(dataSource).withTableName(DOWNLOAD_TABLE);
        this.viewed = Collections.synchronizedMap(new HashMap<>());
        this.downloaded = Collections.synchronizedMap(new HashMap<>());

        for (val table : List.of(VIEW_TABLE, DOWNLOAD_TABLE)) {
            jdbcTemplate.execute(CREATE_STATEMENT.formatted(table));
        }
    }

    @Override
    public void recordView(@NonNull String uuid, @NonNull String addr) {
        synchronized (viewed) {
            recordMetric(viewed, uuid, addr);
        }
    }

    @Override
    public void recordDownload(@NonNull String uuid, @NonNull String addr) {
        synchronized (downloaded) {
            recordMetric(downloaded, uuid, addr);
        }
    }

    @Override
    public int totalViews(@NonNull String uuid) {
        return totalAmount(VIEW_TABLE, uuid);
    }

    @Override
    public int totalDownloads(@NonNull String uuid) {
        return totalAmount(DOWNLOAD_TABLE, uuid);
    }

    @Scheduled(initialDelay=TimeConstants.ONE_HOUR, fixedDelay=TimeConstants.ONE_HOUR)
    public void syncDB() {
        log.info("Exporting metric counts");

        synchronized (viewed) {
            viewed.forEach((doc, viewers) ->
                viewInserter.execute(Map.of(
                    "start_timestamp", lastRun,
                    "end_timestamp", Instant.now().getEpochSecond(),
                    "amount", viewers.size(),
                    "document", doc
                ))
            );
            viewed.clear();
        }

        synchronized (downloaded) {
            downloaded.forEach((doc, downloaders) ->
                downloadInserter.execute(Map.of(
                    "start_timestamp", lastRun,
                    "end_timestamp", Instant.now().getEpochSecond(),
                    "amount", downloaders.size(),
                    "document", doc
                ))
            );
            downloaded.clear();
        }

        lastRun = Instant.now().getEpochSecond();
    }

    private void recordMetric(@NonNull Map<String, Set<String>> map, @NonNull String uuid, @NonNull String addr) {
        map.computeIfAbsent(uuid, k -> new HashSet<>()).add(addr);
    }

    private int totalAmount(@NonNull String table, @NonNull String uuid) {
        return jdbcTemplate.queryForObject(TOTAL_STATEMENT.formatted(table), Integer.class, uuid);
    }
}
