package uk.ac.ceh.gateway.catalogue.csp;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/**
 * Receives the violations browsers report against the {@link ContentSecurityPolicy} and logs them, so the
 * report-only policy can be tuned from what real pages do before it's enforced (NERC-CEH/dri-one#454).
 *
 * <p>Browsers send reports anonymously, so the security chains let anyone POST here
 * ({@link #PATH}). What arrives is therefore untrusted: the body is capped, only a few fields are logged,
 * each one shortened, and at most {@link #LOGGED_PER_MINUTE} reports a minute are logged, with a count of
 * the rest.</p>
 *
 * <p>Two formats arrive: {@code application/csp-report} ({@code report-uri}: one object under
 * {@code "csp-report"}, hyphenated keys) and {@code application/reports+json} (the Reporting API behind
 * {@code report-to}: an array of reports, camel-case keys under {@code "body"}).</p>
 */
@Slf4j
@RestController
public class CspReportController {

    public static final String PATH = "/csp-reports";
    static final int MAX_BODY_BYTES = 64 * 1024;
    static final int LOGGED_PER_MINUTE = 60;
    private static final int MAX_FIELD_LENGTH = 200;

    private final ObjectMapper mapper;
    private final Clock clock;
    private long minute = -1;
    private int loggedThisMinute;
    private int suppressedThisMinute;

    @Autowired
    public CspReportController(ObjectMapper mapper) {
        this(mapper, Clock.systemUTC());
    }

    CspReportController(ObjectMapper mapper, Clock clock) {
        this.mapper = mapper;
        this.clock = clock;
    }

    @PostMapping(PATH)
    public ResponseEntity<Void> report(HttpServletRequest request) throws IOException {
        byte[] body;
        try (InputStream in = request.getInputStream()) {
            body = in.readNBytes(MAX_BODY_BYTES + 1);
        }
        if (body.length > MAX_BODY_BYTES) {
            return ResponseEntity.status(413).build();
        }
        List<String> violations;
        try {
            violations = violations(mapper.readTree(body));
        } catch (JacksonException e) {
            return ResponseEntity.badRequest().build();
        }
        violations.forEach(this::log);
        return ResponseEntity.noContent().build();
    }

    static List<String> violations(JsonNode root) {
        List<String> found = new ArrayList<>();
        if (root.has("csp-report")) {
            JsonNode report = root.get("csp-report");
            found.add(describe(
                text(report, "effective-directive", "violated-directive"),
                text(report, "blocked-uri"),
                text(report, "document-uri"),
                text(report, "source-file"),
                text(report, "line-number")));
        } else if (root.isArray()) {
            for (JsonNode report : root) {
                if (!"csp-violation".equals(report.path("type").asString(""))) {
                    continue;
                }
                JsonNode body = report.path("body");
                found.add(describe(
                    text(body, "effectiveDirective"),
                    text(body, "blockedURL"),
                    text(body, "documentURL"),
                    text(body, "sourceFile"),
                    text(body, "lineNumber")));
            }
        }
        return found;
    }

    private static String describe(String directive, String blocked, String document, String source, String line) {
        String where = source.isEmpty() ? "" : " (" + source + (line.isEmpty() ? "" : ":" + line) + ")";
        return directive + " would block " + (blocked.isEmpty() ? "inline" : blocked) + " on " + document + where;
    }

    /** The first of these fields present, shortened, with control characters removed so it can't forge log lines. */
    private static String text(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value != null && !value.isNull()) {
                String text = value.asString("").replaceAll("\\p{Cntrl}", " ");
                return text.length() > MAX_FIELD_LENGTH ? text.substring(0, MAX_FIELD_LENGTH) + "…" : text;
            }
        }
        return "";
    }

    private synchronized void log(String violation) {
        long now = clock.millis() / 60_000;
        if (now != minute) {
            if (suppressedThisMinute > 0) {
                log.warn("CSP: {} more violation reports in the previous minute were not logged", suppressedThisMinute);
            }
            minute = now;
            loggedThisMinute = 0;
            suppressedThisMinute = 0;
        }
        if (loggedThisMinute < LOGGED_PER_MINUTE) {
            loggedThisMinute++;
            log.warn("CSP: {}", violation);
        } else {
            suppressedThisMinute++;
        }
    }
}
