package uk.ac.ceh.gateway.catalogue.maintenance;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.controllers.DocumentController;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.metrics.MetricsService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Deletes any record, in any catalogue, for holders of
 * {@link DocumentController#ADMIN_DELETE_ROLE} — ignoring that record's own {@code permissions}.
 *
 * <p>This exists so records orphaned by a retired document type or catalogue can be removed through the
 * application rather than by editing git on the SAN, which would bypass cache eviction, index updates
 * and any audit trail. The ordinary {@code DELETE /documents/{id}} is deliberately unchanged: widening
 * it would let the editor's delete button destroy anyone's record on a mis-click.</p>
 *
 * <p>Because the capability is broad, deletion takes three steps — form, preview, confirm — and the
 * confirm step requires the id retyped, a reason, and for a published record the catalogue id as well.
 * CSRF is enabled for this route alone; see {@code SecurityConfig}.</p>
 *
 * <p>HTML only. This is deliberately a form and not an API: the safeguards are the point, and a JSON
 * caller would be a way to skip them.</p>
 */
@Hidden
@Slf4j
@ToString(onlyExplicitlyIncluded = true)
@Controller
@RequestMapping(value = "maintenance/documents/delete", produces = MediaType.TEXT_HTML_VALUE)
@Secured(DocumentController.ADMIN_DELETE_ROLE)
public class AdminDeleteController {

    /** Ids are UUIDs. Anything else — including a path fragment — is rejected before use. */
    private static final Pattern ID_PATTERN =
        Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private static final int MAX_REASON_LENGTH = 200;

    private final CachedDataRepository cachedDataRepository;
    private final DocumentInfoMapper<MetadataInfo> metadataInfoMapper;
    private final DocumentTypeLookupService documentTypeLookupService;
    private final DocumentRepository documentRepository;

    /**
     * {@code null} outside the {@code metrics} profile, matching {@code DownloadController}'s handling
     * of the same profile-gated bean. Every use below is null-checked rather than assuming presence.
     */
    private final @Nullable MetricsService metricsService;

    public AdminDeleteController(
        CachedDataRepository cachedDataRepository,
        DocumentInfoMapper<MetadataInfo> metadataInfoMapper,
        DocumentTypeLookupService documentTypeLookupService,
        DocumentRepository documentRepository,
        @Nullable MetricsService metricsService
    ) {
        this.cachedDataRepository = cachedDataRepository;
        this.metadataInfoMapper = metadataInfoMapper;
        this.documentTypeLookupService = documentTypeLookupService;
        this.documentRepository = documentRepository;
        this.metricsService = metricsService;
        log.info("Creating");
    }

    @GetMapping
    @ResponseBody
    public AdminDeleteResponse form(CsrfToken csrfToken) {
        AdminDeleteResponse response = new AdminDeleteResponse();
        response.setLocation(AdminDeleteLocation.METADATA_RECORD);
        return withCsrf(response, csrfToken);
    }

    /**
     * Reports what a record is, without touching it. Reads {@code .meta} only, which parses even when
     * {@code documentType} has no registered class — the case this feature exists for.
     */
    @PostMapping("preview")
    @ResponseBody
    public AdminDeleteResponse preview(
        CsrfToken csrfToken,
        @RequestParam AdminDeleteLocation location,
        @RequestParam String id
    ) {
        AdminDeleteResponse response = withCsrf(new AdminDeleteResponse(), csrfToken);
        response.setLocation(location);
        response.setId(id);

        if (!isValidId(id)) {
            response.setError("That is not a valid record id. Ids are UUIDs.");
            return response;
        }
        return describe(response, location, id.trim());
    }

    @PostMapping("confirm")
    @ResponseBody
    public AdminDeleteResponse confirm(
        @ActiveUser CatalogueUser user,
        CsrfToken csrfToken,
        @RequestParam AdminDeleteLocation location,
        @RequestParam String id,
        @RequestParam(defaultValue = "") String confirmId,
        @RequestParam(defaultValue = "") String reason,
        @RequestParam(defaultValue = "") String confirmCatalogue
    ) {
        AdminDeleteResponse response = withCsrf(new AdminDeleteResponse(), csrfToken);
        response.setLocation(location);
        response.setId(id);

        if (!isValidId(id)) {
            response.setError("That is not a valid record id. Ids are UUIDs.");
            return response;
        }
        String trimmedId = id.trim();

        // Describe first, so a rejected confirmation still shows what the record is and the operator can
        // correct the form rather than starting again.
        describe(response, location, trimmedId);
        if (!response.isFound()) {
            return response;
        }

        if (!trimmedId.equals(confirmId.trim())) {
            response.setError("The retyped id does not match. Nothing was deleted.");
            return response;
        }
        String cleanReason = cleanReason(reason);
        if (cleanReason.isEmpty()) {
            response.setError("A reason is required. Nothing was deleted.");
            return response;
        }
        if (response.isPublished() && !response.getCatalogue().equalsIgnoreCase(confirmCatalogue.trim())) {
            response.setError(
                "This record is published, so the catalogue id must be typed to confirm. Nothing was deleted.");
            return response;
        }

        return performDelete(response, user, location.pathFor(trimmedId), cleanReason);
    }

    /**
     * Deletes the git document and the metrics rows independently: neither's absence or failure blocks
     * the other, so this also serves the metrics-only orphan (no document, dangling {@code views}/
     * {@code downloads} rows — see #252) as well as the reverse. Overall success is "at least one of the
     * two actually removed something", not "both succeeded".
     */
    private AdminDeleteResponse performDelete(
        AdminDeleteResponse response, CatalogueUser user, String path, String reason
    ) {
        List<String> failures = new ArrayList<>();
        boolean anyDeleted = false;

        if (response.isDocumentFound()) {
            String subject = response.isPublished() ? "PUBLISHED document" : "document";
            String message = "admin delete %s: %s (reason: %s)".formatted(subject, path, reason);
            // Warn rather than info: an administrative deletion bypassing a record's own permissions
            // should be visible in log aggregation, not only in the datastore's git history.
            log.warn("Admin delete by {}: {} (reason: {})", user.getUsername(), path, reason);
            try {
                documentRepository.delete(user, path, message);
                response.addMessage("Deleted %s.".formatted(path));
                anyDeleted = true;
            } catch (Exception ex) {
                log.error("Admin delete of {} by {} failed", path, user.getUsername(), ex);
                failures.add("Could not delete %s: %s".formatted(path, ex.getMessage()));
            }
        }

        if (metricsService != null && response.isMetricsFound()) {
            String id = response.getId();
            try {
                metricsService.deleteMetricsFor(id);
                response.addMessage("Deleted metrics for %s.".formatted(id));
                anyDeleted = true;
            } catch (Exception ex) {
                log.error("Admin delete of metrics for {} by {} failed", id, user.getUsername(), ex);
                failures.add("Could not delete metrics for %s: %s".formatted(id, ex.getMessage()));
            }
        }

        if (!failures.isEmpty()) {
            response.setError(String.join(" ", failures));
        }
        if (anyDeleted) {
            response.setDeleted(true);
            response.setFound(false);
            response.setDocumentFound(false);
            response.setMetricsFound(false);
            // Clear the fields that identified the now-deleted record, so the re-rendered lookup form
            // doesn't leave a stale id sitting in step 1 ready to be resubmitted by mistake.
            response.setId(null);
            response.setPath(null);
            response.setDocumentType(null);
            response.setState(null);
            response.setCatalogue(null);
            response.setPermissions(null);
        }
        return response;
    }

    /**
     * Populates the {@code .meta} facts, leaving {@code found} false only if there is neither a git
     * document nor any metrics recorded for {@code id}. An id with metrics but no document is still
     * {@code found} — the metrics-only orphan case #252 exists for — just with {@code documentFound}
     * false and none of the document-specific fields populated.
     */
    private AdminDeleteResponse describe(
        AdminDeleteResponse response, AdminDeleteLocation location, String id
    ) {
        String path = location.pathFor(id);
        response.setPath(path);
        boolean metricsPresent = metricsService != null && metricsService.hasMetricsFor(id);
        response.setMetricsFound(metricsPresent);

        MetadataInfo info;
        try {
            String revision = cachedDataRepository.getLatestRevisionId();
            byte[] meta = cachedDataRepository.readLatest(revision, path + ".meta");
            info = metadataInfoMapper.readInfo(new ByteArrayInputStream(meta));
            response.setRawPresent(true);
            try {
                response.setRawSize(cachedDataRepository.readLatest(revision, path + ".raw").length);
            } catch (IOException | RuntimeException noRaw) {
                // A .meta with no .raw is a real state in the datastore, not an error: most of the
                // orphaned records left by retired catalogues have no body at all.
                response.setRawPresent(false);
            }
        } catch (IOException | RuntimeException ex) {
            if (metricsPresent) {
                // No document, but there is something else to clean up: let the form proceed to confirm
                // deleting the metrics alone rather than reporting a false "not found".
                response.setFound(true);
                return response;
            }
            // Logged, unlike the .raw catch below: that one is a known, benign state (most orphans have
            // no body), but this one also catches a .meta that exists yet fails to parse — exactly the
            // kind of damaged record this feature exists to clear up. Without a log line, that case was
            // indistinguishable from a genuinely missing record, both to the operator and to anyone
            // trying to diagnose it afterwards.
            log.warn("Admin delete: no usable record at {}: {}", path, ex.toString());
            response.setError("No record found at %s.".formatted(path));
            return response;
        }

        response.setFound(true);
        response.setDocumentFound(true);
        response.setDocumentType(info.getDocumentType());
        response.setDocumentTypeRegistered(isTypeRegistered(info.getDocumentType()));
        response.setState(info.getState());
        response.setCatalogue(info.getCatalogue());
        response.setPermissions(summarise(info));
        return response;
    }

    private boolean isTypeRegistered(String documentType) {
        if (documentType == null) {
            return false;
        }
        try {
            documentTypeLookupService.getType(documentType);
            return true;
        } catch (IllegalArgumentException notRegistered) {
            // This is the orphan signature - the record cannot be deserialised, which is why it needs
            // removing and why the preview reads .meta only.
            return false;
        }
    }

    private String summarise(MetadataInfo info) {
        return Arrays.stream(Permission.values())
            .map(permission -> "%s: %s".formatted(permission, String.join(", ", info.getIdentities(permission))))
            .collect(Collectors.joining("; "));
    }

    private boolean isValidId(String id) {
        return id != null && ID_PATTERN.matcher(id.trim()).matches();
    }

    /** Collapses whitespace and caps the length, so the reason cannot reshape the commit message. */
    private String cleanReason(String reason) {
        if (reason == null) {
            return "";
        }
        String collapsed = reason.replaceAll("\\s+", " ").trim();
        return collapsed.length() > MAX_REASON_LENGTH ? collapsed.substring(0, MAX_REASON_LENGTH) : collapsed;
    }

    private AdminDeleteResponse withCsrf(AdminDeleteResponse response, CsrfToken csrfToken) {
        if (csrfToken != null) {
            response.setCsrfParameterName(csrfToken.getParameterName());
            // Spring Security 6 defers token generation; reading it here materialises and persists it so
            // the form can carry it.
            response.setCsrfToken(csrfToken.getToken());
        }
        return response;
    }
}
