package uk.ac.ceh.gateway.catalogue.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.TimeConstants;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

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
    private final DocumentRepository documentRepository;

    // SQLite has no built-in datetime type, so we store dates as Unix timestamps (seconds since 1 Jan 1970)
    private static final String CREATE_STATEMENT = """
        CREATE TABLE IF NOT EXISTS %s (
            start_timestamp integer NOT NULL,
            end_timestamp integer NOT NULL,
            amount integer NOT NULL,
            document text NOT NULL,
            doc_title text NOT NULL,
            record_type text NOT NULL
        )
        """;
    private static final String TOTAL_STATEMENT = "SELECT coalesce(sum(amount), 0) FROM %s WHERE document = ?";
    private static final String UPDATE_STATEMENT = "UPDATE %s SET doc_title = ?, record_type = ? WHERE document = ?";
    private static final String DISTINCT_DOCS_QUERY = "SELECT DISTINCT document FROM %s";
    private static final String VIEW_TABLE = "views";
    private static final String DOWNLOAD_TABLE = "downloads";

    public JDBCMetricsService(@NonNull DataSource dataSource,
                              DocumentRepository documentRepository) {
        log.info("Creating {}", this);
        this.documentRepository = documentRepository;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.viewInserter = new SimpleJdbcInsert(dataSource).withTableName(VIEW_TABLE);
        this.downloadInserter = new SimpleJdbcInsert(dataSource).withTableName(DOWNLOAD_TABLE);
        this.viewed = Collections.synchronizedMap(new HashMap<>());
        this.downloaded = Collections.synchronizedMap(new HashMap<>());

        List.of(VIEW_TABLE, DOWNLOAD_TABLE).forEach(table -> jdbcTemplate.execute(CREATE_STATEMENT.formatted(table)));
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

    @Scheduled(initialDelay=TimeConstants.ONE_MINUTE, fixedDelay=TimeConstants.ONE_MINUTE)
    public void syncDB() {
        log.info("Exporting metric counts");
            syncDBHelper(viewed,viewInserter);
            syncDBHelper(downloaded,downloadInserter);
//        synchronized (viewed) {
////            viewed.forEach((doc, viewers) ->
////                viewInserter.execute(Map.of(
////                    "start_timestamp", lastRun,
////                    "end_timestamp", Instant.now().getEpochSecond(),
////                    "amount", viewers.size(),
////                    "document", doc,
////                    "doc_title", "poopy",
////                    "record_type", "beans"
////                ))
////            );
//            for (Map.Entry<String, Set<String>> metricInfo : viewed.entrySet()) {
//                String doc = metricInfo.getKey();
//                Set<String> viewers = metricInfo.getValue();
//                MetadataDocument document;
//                try {
//                    document = documentRepository.read(doc);
//                    log.info("document title {}", document.getTitle());
//                } catch (Exception e) {
//                    log.error("Error reading document from repository {}", doc, e);
//                    continue;
//                }
//                viewInserter.execute(Map.of(
//                    "start_timestamp", lastRun,
//                    "end_timestamp", Instant.now().getEpochSecond(),
//                    "amount", viewers.size(),
//                    "document", doc,
//                    "doc_title", document.getTitle(),
//                    "record_type", document.getType()
//                ));
//            }
//            viewed.clear();
//        }

//        synchronized (downloaded) {
//            downloaded.forEach((doc, downloaders) ->
//                downloadInserter.execute(Map.of(
//                    "start_timestamp", lastRun,
//                    "end_timestamp", Instant.now().getEpochSecond(),
//                    "amount", downloaders.size(),
//                    "document", doc,
//                    "doc_title", "doccy mcdocface",
//                    "record_type", "choccy woccy doo da"
//                ))
//            );
//            downloaded.clear();
//        }

        lastRun = Instant.now().getEpochSecond();
    }

    @Scheduled(initialDelay=120_000, fixedDelay=120_000)
    public void updateDB() {
        log.info("Updating document titles and record types");
        updateDBHelper(VIEW_TABLE);
        updateDBHelper(DOWNLOAD_TABLE);

    }

    private void recordMetric(@NonNull Map<String, Set<String>> map, @NonNull String uuid, @NonNull String addr) {
        map.computeIfAbsent(uuid, k -> new HashSet<>()).add(addr);
    }

    private int totalAmount(@NonNull String table, @NonNull String uuid) {
        return jdbcTemplate.queryForObject(TOTAL_STATEMENT.formatted(table), Integer.class, uuid);
    }

    private void updateDBHelper(String table) {
        List<String> distinctDocs = jdbcTemplate.queryForList(DISTINCT_DOCS_QUERY.formatted(table), String.class);
        distinctDocs.forEach((doc) -> {
            MetadataDocument document = new GeminiDocument();
            try {
                document = documentRepository.read(doc);
            } catch (Exception e) {
                log.error("Error reading document from repository {}", doc, e);
            }
            log.info("DOING THE UPDATE ON {}", table);
            jdbcTemplate.update(UPDATE_STATEMENT.formatted(table), document.getTitle(), document.getType(), doc);
        });
    }

    private void syncDBHelper(Map<String, Set<String>> tableMap, SimpleJdbcInsert inserter) {
        synchronized (tableMap) {
            for (Map.Entry<String, Set<String>> metricInfo : tableMap.entrySet()) {
                String doc = metricInfo.getKey();
                Set<String> viewers = metricInfo.getValue();
                MetadataDocument document;
                try {
                    document = documentRepository.read(doc);
                    log.info("document title {}", document.getTitle());
                } catch (Exception e) {
                    log.error("Error reading document from repository {}", doc, e);
                    continue;
                }
                inserter.execute(Map.of(
                    "start_timestamp", lastRun,
                    "end_timestamp", Instant.now().getEpochSecond(),
                    "amount", viewers.size(),
                    "document", doc,
                    "doc_title", document.getTitle(),
                    "record_type", document.getType()
                ));
            }
            tableMap.clear();
        }
    }
}
