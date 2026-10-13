package uk.ac.ceh.gateway.catalogue.csp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CspReportController")
@ExtendWith(OutputCaptureExtension.class)
class CspReportControllerTest {

    private static final String REPORT_URI_BODY = """
        {"csp-report": {
          "document-uri": "https://catalogue.ceh.ac.uk/documents/abc",
          "violated-directive": "script-src-elem",
          "effective-directive": "script-src-elem",
          "blocked-uri": "https://evil.example/x.js",
          "source-file": "https://catalogue.ceh.ac.uk/documents/abc",
          "line-number": 12
        }}""";

    private static final String REPORTING_API_BODY = """
        [{"type": "csp-violation", "url": "https://catalogue.ceh.ac.uk/eidc/documents",
          "body": {"documentURL": "https://catalogue.ceh.ac.uk/eidc/documents", "effectiveDirective": "img-src",
                   "blockedURL": "http://insecure.example/a.png", "disposition": "report"}},
         {"type": "deprecation", "body": {"message": "not a CSP report"}}]""";

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-13T12:00:00Z"), ZoneOffset.UTC);
    private final CspReportController controller = new CspReportController(JsonMapper.builder().build(), clock);

    private int post(String body) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", CspReportController.PATH);
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return controller.report(request).getStatusCode().value();
    }

    @Test
    @DisplayName("logs a report-uri report")
    void logsReportUri(CapturedOutput output) throws Exception {
        assertThat(post(REPORT_URI_BODY)).isEqualTo(204);

        assertThat(output.getOut()).contains(
            "CSP: script-src-elem would block https://evil.example/x.js on https://catalogue.ceh.ac.uk/documents/abc "
                + "(https://catalogue.ceh.ac.uk/documents/abc:12)");
    }

    @Test
    @DisplayName("logs the CSP reports in a Reporting API batch and ignores the others")
    void logsReportingApiBatch(CapturedOutput output) throws Exception {
        assertThat(post(REPORTING_API_BODY)).isEqualTo(204);

        assertThat(output.getOut())
            .contains("CSP: img-src would block http://insecure.example/a.png on https://catalogue.ceh.ac.uk/eidc/documents")
            .doesNotContain("not a CSP report");
    }

    @Test
    @DisplayName("refuses a body that isn't JSON, or is too big, without logging it")
    void refusesBadBodies(CapturedOutput output) throws Exception {
        assertThat(post("not json")).isEqualTo(400);
        assertThat(post("[" + " ".repeat(CspReportController.MAX_BODY_BYTES) + "]")).isEqualTo(413);

        assertThat(output.getOut()).doesNotContain("CSP:");
    }

    @Test
    @DisplayName("can't forge log lines: control characters are removed and fields shortened")
    void sanitisesWhatItLogs(CapturedOutput output) throws Exception {
        String forged = "{\"csp-report\": {\"effective-directive\": \"img-src\", \"document-uri\": \"x\", "
            + "\"blocked-uri\": \"https://a.example/\\nERROR fake line " + "y".repeat(500) + "\"}}";

        post(forged);

        assertThat(output.getOut()).doesNotContain("\nERROR fake line").contains("https://a.example/ ERROR fake line");
        assertThat(output.getOut()).doesNotContain("y".repeat(250));
    }

    @Test
    @DisplayName("logs at most a minute's worth of reports, then counts the rest")
    void capsTheLoggingRate(CapturedOutput output) throws Exception {
        for (int i = 0; i < CspReportController.LOGGED_PER_MINUTE + 5; i++) {
            post(REPORT_URI_BODY);
        }

        long logged = output.getOut().lines().filter(line -> line.contains("CSP: script-src-elem")).count();
        assertThat(logged).isEqualTo(CspReportController.LOGGED_PER_MINUTE);
    }
}
