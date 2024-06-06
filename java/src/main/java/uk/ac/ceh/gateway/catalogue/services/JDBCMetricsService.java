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
    @NonNull private Map<String, Set<String>> viewed;
    @NonNull private Map<String, Set<String>> downloaded;
    @NonNull private SimpleJdbcInsert simpleJdbcInsert;
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
    private static final String VIEW_TABLE = "views";
    private static final String DOWNLOAD_TABLE = "downloads";

    public JDBCMetricsService(@NonNull DataSource dataSource) {
        log.info("Creating {}", this);
        val jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource);
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

    @Scheduled(initialDelay=TimeConstants.ONE_HOUR, fixedDelay=TimeConstants.ONE_HOUR)
    public void syncDB() {
        log.info("Exporting metric counts");

        synchronized (viewed) {
            viewed.forEach((doc, viewers) ->
                simpleJdbcInsert.withTableName(VIEW_TABLE)
                    .execute(Map.of(
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
                simpleJdbcInsert.withTableName(DOWNLOAD_TABLE)
                    .execute(Map.of(
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
}
