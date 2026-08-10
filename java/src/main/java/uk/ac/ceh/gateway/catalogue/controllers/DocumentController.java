package uk.ac.ceh.gateway.catalogue.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringActivity;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringProgramme;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.metrics.MetricsService;
import uk.ac.ceh.gateway.catalogue.serviceagreement.GitRepoServiceAgreementService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.*;
import static uk.ac.ceh.gateway.catalogue.model.Permission.VIEW;

@SuppressWarnings("SpringMVCViewInspection")
@Slf4j
@ToString(callSuper = true)
@Controller
public class DocumentController extends AbstractDocumentController {
    public static final String MAINTENANCE_ROLE = "ROLE_CIG_SYSTEM_ADMIN";

    /**
     * Grants deletion of <em>any</em> record, in any catalogue, ignoring that record's own
     * {@code permissions}. Held deliberately narrow and separate from {@link #MAINTENANCE_ROLE}: it
     * exists so orphaned records left by a retired document type or catalogue can be cleaned up through
     * the application rather than by editing git on the SAN. The only route that honours it is the
     * guarded admin delete form; the ordinary {@code DELETE /documents/{id}} still requires DELETE on
     * the record itself.
     */
    public static final String ADMIN_DELETE_ROLE = "ROLE_CIG_ADMIN_DELETE";
    private final MetricsService metricsService;
    private final List<String> metricsExcludedUsers;
    private final JenaLookupService jenaService;

    public DocumentController(
        @Nullable MetricsService metricsService,
        @Value("#{'${metrics.users.excluded}'.split(',')}") List<String> metricExcludedUsers,
        DocumentRepository documentRepository,
        JenaLookupService jenaService,
        CachedDataRepository cachedDataRepository
    ) {
        super(documentRepository, cachedDataRepository);
        this.metricsService = metricsService;
        this.metricsExcludedUsers = metricExcludedUsers;
        this.jenaService = jenaService;
        log.info("Creating");
    }

    @RequestMapping (value = "documents/upload",
    method = RequestMethod.GET)
        public String uploadForm() {
            return "html/upload";
        }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
    method = RequestMethod.POST,
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Object uploadFile(
            @ActiveUser CatalogueUser user,
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("type") String documentType,
            @RequestParam("catalogue") String catalogue
            ) throws DocumentRepositoryException, IOException  {
        val data = documentRepository.save(
                user,
                multipartFile.getInputStream(),
                MediaType.parseMediaType(
                    Objects.requireNonNull(multipartFile.getContentType())
                    ),
                documentType,
                catalogue,
                "new file upload"
                );
        log.debug("Document URI: {}", data.getUri());
        return new RedirectView(data.getUri());
            }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
    method = RequestMethod.POST,
    consumes = GEMINI_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newGeminiDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody GeminiDocument document,
            @RequestParam("catalogue") String catalogue
            ) throws DocumentRepositoryException {
        return saveNewMetadataDocument(
                user,
                document,
                catalogue,
                "new Gemini Document"
                );
            }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
    method = RequestMethod.PUT,
    consumes = GEMINI_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateGeminiDocument(
        @ActiveUser CatalogueUser user,
        @PathVariable String file,
        @RequestBody GeminiDocument document,
        @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
    ) throws DocumentRepositoryException, IOException {
        return saveMetadataDocument(
                user,
                file,
                document,
                ifMatch
                );
            }


@PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
        method = RequestMethod.POST,
        consumes = MONITORING_ACTIVITY_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newMonitoringActivity(
        @ActiveUser CatalogueUser user,
        @RequestBody MonitoringActivity document,
        @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new Monitoring activity"
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
        method = RequestMethod.PUT,
        consumes = MONITORING_ACTIVITY_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateMonitoringActivity(
        @ActiveUser CatalogueUser user,
        @PathVariable String file,
        @RequestBody MonitoringActivity document,
        @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
    ) throws DocumentRepositoryException, IOException {
        return saveMetadataDocument(
            user,
            file,
            document,
            ifMatch
        );
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
        method = RequestMethod.POST,
        consumes = MONITORING_FACILITY_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newMonitoringFacility(
        @ActiveUser CatalogueUser user,
        @RequestBody MonitoringFacility document,
        @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new Monitoring facility"
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
        method = RequestMethod.PUT,
        consumes = MONITORING_FACILITY_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateMonitoringFacility(
        @ActiveUser CatalogueUser user,
        @PathVariable String file,
        @RequestBody MonitoringFacility document,
        @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
    ) throws DocumentRepositoryException, IOException {
        return saveMetadataDocument(
            user,
            file,
            document,
            ifMatch
        );
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
        method = RequestMethod.POST,
        consumes = MONITORING_NETWORK_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newMonitoringNetwork(
        @ActiveUser CatalogueUser user,
        @RequestBody MonitoringNetwork document,
        @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new Monitoring Network"
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
        method = RequestMethod.PUT,
        consumes = MONITORING_NETWORK_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateMonitoringNetwork(
        @ActiveUser CatalogueUser user,
        @PathVariable String file,
        @RequestBody MonitoringNetwork document,
        @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
    ) throws DocumentRepositoryException, IOException {
        return saveMetadataDocument(
            user,
            file,
            document,
            ifMatch
        );
    }
    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
        method = RequestMethod.POST,
        consumes = MONITORING_PROGRAMME_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newMonitoringProgramme(
        @ActiveUser CatalogueUser user,
        @RequestBody MonitoringProgramme document,
        @RequestParam("catalogue") String catalogue
    ) throws DocumentRepositoryException {
        return saveNewMetadataDocument(
            user,
            document,
            catalogue,
            "new Monitoring programme"
        );
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
        method = RequestMethod.PUT,
        consumes = MONITORING_PROGRAMME_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateMonitoringProgramme(
        @ActiveUser CatalogueUser user,
        @PathVariable String file,
        @RequestBody MonitoringProgramme document,
        @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
    ) throws DocumentRepositoryException, IOException {
        return saveMetadataDocument(
            user,
            file,
            document,
            ifMatch
        );
    }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
    method = RequestMethod.POST,
    consumes = CEH_MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newCehModelDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody CehModel document,
            @RequestParam("catalogue") String catalogue
            ) throws DocumentRepositoryException {
        return saveNewMetadataDocument(
                user,
                document,
                catalogue,
                "new CEH Model document"
                );
            }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
    method = RequestMethod.PUT,
    consumes = CEH_MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateCehModelDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable String file,
            @RequestBody CehModel document,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
            ) throws DocumentRepositoryException, IOException {
        return saveMetadataDocument(
                user,
                file,
                document,
                ifMatch
                );
            }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
    method = RequestMethod.POST,
    consumes = DATA_TYPE_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newDataType(
            @ActiveUser CatalogueUser user,
            @RequestBody DataType document,
            @RequestParam("catalogue") String catalogue
            ) throws DocumentRepositoryException {
        return saveNewMetadataDocument(
                user,
                document,
                catalogue,
                "new Data type"
                );
            }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
    method = RequestMethod.PUT,
    consumes = DATA_TYPE_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateDataType(
            @ActiveUser CatalogueUser user,
            @PathVariable String file,
            @RequestBody DataType document,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
            ) throws DocumentRepositoryException, IOException {
        return saveMetadataDocument(
                user,
                file,
                document,
                ifMatch
                );
            }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
    method = RequestMethod.POST,
    consumes = CEH_MODEL_APPLICATION_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newCehModelApplicationDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody CehModelApplication document,
            @RequestParam("catalogue") String catalogue
            ) throws DocumentRepositoryException {
        return saveNewMetadataDocument(
                user,
                document,
                catalogue,
                "new CEH Model document"
                );
            }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
    method = RequestMethod.PUT,
    consumes = CEH_MODEL_APPLICATION_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateCehModelApplicationDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable String file,
            @RequestBody CehModelApplication document,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
            ) throws DocumentRepositoryException, IOException {
        return saveMetadataDocument(
                user,
                file,
                document,
                ifMatch
                );
            }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
    method = RequestMethod.POST,
    consumes = LINKED_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newLinkDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody LinkDocument document,
            @RequestParam("catalogue") String catalogue
            ) throws DocumentRepositoryException {
        return saveNewMetadataDocument(
                user,
                document,
                catalogue,
                "new Linked Document"
                );
            }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping(value = "documents/{file}",
    method = RequestMethod.PUT,
    consumes = LINKED_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateLinkDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable String file,
            @RequestBody LinkDocument document,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
            ) throws DocumentRepositoryException, IOException {
        return saveMetadataDocument(
                user,
                file,
                document,
                ifMatch
                );
            }

    private MetadataDocument addJenaRelationships(MetadataDocument document) {
        switch (document) {
            case GeminiDocument doc -> doc.populateFromJenaService(jenaService);
            case MonitoringActivity doc -> doc.populateFromJenaService(jenaService);
            case MonitoringFacility doc -> doc.populateFromJenaService(jenaService);
            case MonitoringNetwork doc -> doc.populateFromJenaService(jenaService);
            case MonitoringProgramme doc -> doc.populateFromJenaService(jenaService);
            default -> {}
        }
        return document;
    }

    @CrossOrigin
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @GetMapping("documents/{file}")
    public ResponseEntity<MetadataDocument> readMetadata(
            @ActiveUser CatalogueUser user,
            @PathVariable String file,
            HttpServletRequest request
        ) throws DocumentRepositoryException, IOException {
        MetadataDocument document = documentRepository.read(file);
        if(metricsService != null && !metricsExcludedUsers.contains(user.getUsername()) && !document.getState().equals(GitRepoServiceAgreementService.DRAFT)) {
            metricsService.recordView(file, request.getRemoteAddr());
        }
        MetadataDocument body = postProcessLinkDocument(addJenaRelationships(document));
        String revision = cachedDataRepository.getDocumentRevisionToken(file);
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (revision != null) {
            builder.eTag(revision); // Spring quotes this into a strong ETag: "revision"
        }
        return builder.body(body);
    }

    @CrossOrigin
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @GetMapping("documents/{file}.xml")
    public String readMetadataXml(
            @ActiveUser CatalogueUser user,
            @PathVariable String file
            ) {
        return "forward:/documents/" + file + "?format=" + GEMINI_XML_SHORT;
    }

    @ResponseBody
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @GetMapping(value = "documents/{file}", produces = LINKED_JSON_VALUE)
    public MetadataDocument readLinkDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable String file
            ) throws DocumentRepositoryException {
        var document = documentRepository.read(file);
        return addJenaRelationships(document);
    }


    @ResponseBody
    @PreAuthorize("@permission.toAccess(#user, #file, #revision, 'VIEW')")
    @GetMapping(value = "history/{revision}/{file}")
    public MetadataDocument readMetadata(
            @ActiveUser CatalogueUser user,
            @PathVariable String file,
            @PathVariable String revision
            ) throws DocumentRepositoryException {
        var document = documentRepository.read(file, revision);
        return postProcessLinkDocument(addJenaRelationships(document));
    }

    private MetadataDocument postProcessLinkDocument(MetadataDocument document) {
        log.debug("processing {}", document.getId());
        if (document instanceof LinkDocument linkDocument) {
            String id = linkDocument.getId();
            String uri = linkDocument.getUri();
            List<Keyword> additionalKeywords = linkDocument.getAdditionalKeywords();
            MetadataInfo metadataInfo = linkDocument.getMetadata();
            MetadataInfo masterMetadataInfo = linkDocument.getOriginal().getMetadata();
            log.debug("publicly viewable: {}", masterMetadataInfo.isPubliclyViewable(VIEW));
            log.debug(masterMetadataInfo.toString());
            if (masterMetadataInfo.isPubliclyViewable(VIEW)) {
                log.debug("Adding linked elements");
                document = linkDocument.getOriginal();
                document.setMetadata(metadataInfo);
                document.setId(id);
                document.setUri(uri);
                document.addAdditionalKeywords(additionalKeywords);
            }
        }
        return document;
    }

    @PreAuthorize("@permission.toAccess(#user, #file, 'DELETE')")
    @RequestMapping(value = "documents/{file}",
    method = RequestMethod.DELETE)
    @ResponseBody
    public DataRevision<CatalogueUser> deleteDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable String file
            ) throws DocumentRepositoryException {
        return documentRepository.delete(user, file);
            }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @PostMapping("documents/{file}/clone")
    public ResponseEntity<Void> cloneAsNewVersion(
        @ActiveUser CatalogueUser user,
        @PathVariable String file
    ) throws DocumentRepositoryException
    {
        MetadataDocument source = documentRepository.read(file);

        if (!(source instanceof GeminiDocument gemini)) {
            return ResponseEntity.notFound().build();
        }

        source.setResourceIdentifiers(Collections.emptyList());
        gemini.setIncomingCitations(Collections.emptyList());
        gemini.setOnlineResources(Collections.emptyList());
        gemini.setCitation(null);
        gemini.setDatasetReferenceDate(null);

        Number version = gemini.getVersion();
        gemini.setVersion(version == null ? 2 : version.intValue() + 1);

        String catalogue = source.getMetadata().getCatalogue();
        MetadataDocument saved = documentRepository.saveNew(user, source, catalogue, String.format("cloned a new version of %s", file));

        URI redirectUri = URI.create("/documents/" + saved.getId());
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(redirectUri).build();
    }
}
