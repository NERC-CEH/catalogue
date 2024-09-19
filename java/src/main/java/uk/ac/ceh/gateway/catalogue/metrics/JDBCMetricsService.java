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
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.time.Instant;
import java.util.*;
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

    @Scheduled(initialDelay=TimeConstants.ONE_HOUR, fixedDelay=TimeConstants.ONE_HOUR)
    public void syncDB() {
        log.info("Exporting metric counts");
        synchronized (viewed) {
            syncDBHelper(viewed, viewInserter);
            viewed.clear();
        }
        synchronized (downloaded) {
            syncDBHelper(downloaded, downloadInserter);
            downloaded.clear();
        }
        lastRun = Instant.now().getEpochSecond();
    }

    @Scheduled(cron = "0 0 1 * * *")
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
            MetadataDocument document;
            try {
                log.info("UPDATING title and type of document ID {} for {} table", doc, table);
                document = documentRepository.read(doc);
                jdbcTemplate.update(UPDATE_STATEMENT.formatted(table), document.getTitle(), document.getType(), doc);
            } catch (Exception e) {
                log.error("Error reading document from repository {}", doc, e);
            }
        });
    }

    private void syncDBHelper(Map<String, Set<String>> tableMap, SimpleJdbcInsert inserter) {
        tableMap.forEach((doc, viewers) -> {
            MetadataDocument document;
            try {
                document = documentRepository.read(doc);
                inserter.execute(Map.of(
                    "start_timestamp", lastRun,
                    "end_timestamp", Instant.now().getEpochSecond(),
                    "amount", viewers.size(),
                    "document", doc,
                    "doc_title", document.getTitle(),
                    "record_type", document.getType()
                ));
            } catch (Exception e) {
                log.error("Error reading document from repository {}", doc, e);
            }
        });
    }

    public List<Map<String,String>> getMetricsReport(Instant startDate, Instant endDate, String orderBy, String ordering, List<String> recordType, String docId, Integer noOfRecords) {
        String sql = "select t.DOCUMENT, ifnull(t.DOC_TITLE, '') as \"DOC_TITLE\", ifnull(t.RECORD_TYPE, '') as \"RECORD_TYPE\", sum(t.VIEWS) as \"VIEWS\", sum(t.DOWNLOADS) as \"DOWNLOADS\" " +
            "from (select DOCUMENT, DOC_TITLE, RECORD_TYPE, AMOUNT as \"DOWNLOADS\", 0 as \"VIEWS\" from downloads where %s " +
            "union select DOCUMENT, DOC_TITLE, RECORD_TYPE, 0 as \"DOWNLOADS\", AMOUNT as \"VIEWS\" from views where %s) t " +
            "group by DOCUMENT,DOC_TITLE,RECORD_TYPE";

        ArrayList<String> whereVal = new ArrayList<>();
        StringBuilder where = new StringBuilder(" 1=1");
        if (startDate != null) {
            String start = Long.toString(startDate.getEpochSecond());
            whereVal.add(start);
            where.append(" and START_TIMESTAMP>=?");
        }
        if (endDate != null) {
            String end = Long.toString(endDate.getEpochSecond());
            whereVal.add(end);
            where.append(" and END_TIMESTAMP<=?");
        }
        if (docId != null && !docId.isBlank()) {
            whereVal.add(String.format("%%%s%%", docId));
            where.append(" and DOCUMENT like ?");
        }
        if (recordType != null && !recordType.isEmpty()) {
            where.append(" and RECORD_TYPE in (");
            for (String type: recordType) {
                whereVal.add(type);
                where.append("?,");
            }
            where.deleteCharAt(where.length() - 1).append(")");
        }

        StringBuilder sqlBuilder = new StringBuilder(sql.formatted(where, where));
        if (orderBy != null && !orderBy.isBlank()) {
            switch (orderBy) {
                case "views":
                    sqlBuilder.append(" order by VIEWS");
                    break;
                case "downloads":
                    sqlBuilder.append(" order by DOWNLOADS");
                    break;
                default:
                    sqlBuilder.append(" order by DOCUMENT");
                    break;
            }
            if (ordering != null && !ordering.isBlank()) {
                if (ordering.equals("descending")) {
                    sqlBuilder.append(" desc");
                }
            }
        }
        if (noOfRecords != null && noOfRecords >= 0) {
            sqlBuilder.append(" limit ").append(noOfRecords);
        } else {
            sqlBuilder.append(" limit 100");
        }

        log.info("Metrics report sql: {}", sqlBuilder);

        return jdbcTemplate.query(
            sqlBuilder.toString(), preparedStatement -> {
                int index = 1;
                int valSize = whereVal.size();
                for (String val: whereVal) {
                    preparedStatement.setString(index, val);
                    preparedStatement.setString(valSize + index, val);
                    index++;
                }
            },
            new ReportMapper()
        );
    }
}
