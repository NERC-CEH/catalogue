package uk.ac.ceh.gateway.catalogue.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.imp.ImpDocument;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.services.MetricsService;

import java.io.IOException;
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
    private final MetricsService metricsService;
    private final List<String> metricsExcludedUsers;

    public DocumentController(
        @Nullable MetricsService metricsService,
        @Value("#{'${metrics.users.excluded}'.split(',')}") List<String> metricExcludedUsers,
        DocumentRepository documentRepository
    ) {
        super(documentRepository);
        this.metricsService = metricsService;
        this.metricsExcludedUsers = metricExcludedUsers;
        log.info("Creating {}", this);
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
    consumes = MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newModelDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody ImpDocument document,
            @RequestParam("catalogue") String catalogue
            ) {
        return saveNewMetadataDocument(
                user,
                document,
                catalogue,
                "new Model Document"
                );
            }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @RequestMapping (value = "documents/{file}",
    method = RequestMethod.PUT,
    consumes = MODEL_JSON_VALUE)
    public ResponseEntity<MetadataDocument> updateModelDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @RequestBody ImpDocument document
            ) {
        return saveMetadataDocument(
                user,
                file,
                document
                );
            }

    @PreAuthorize("@permission.userCanCreate(#catalogue)")
    @RequestMapping (value = "documents",
    method = RequestMethod.POST,
    consumes = GEMINI_JSON_VALUE)
    public ResponseEntity<MetadataDocument> newGeminiDocument(
            @ActiveUser CatalogueUser user,
            @RequestBody GeminiDocument document,
            @RequestParam("catalogue") String catalogue
            ) {
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
            @PathVariable("file") String file,
            @RequestBody GeminiDocument document
            ) {
        return saveMetadataDocument(
                user,
                file,
                document
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
            ) {
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
            @PathVariable("file") String file,
            @RequestBody CehModel document
            ) {
        return saveMetadataDocument(
                user,
                file,
                document
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
            ) {
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
            @PathVariable("file") String file,
            @RequestBody DataType document
            ) {
        return saveMetadataDocument(
                user,
                file,
                document
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
            ) {
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
            @PathVariable("file") String file,
            @RequestBody CehModelApplication document
            ) {
        return saveMetadataDocument(
                user,
                file,
                document
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
            ) {
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
            @PathVariable("file") String file,
            @RequestBody LinkDocument document
            ) {
        return saveMetadataDocument(
                user,
                file,
                document
                );
            }

    @CrossOrigin
    @ResponseBody
    @SneakyThrows
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @GetMapping("documents/{file}")
    public MetadataDocument readMetadata(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            HttpServletRequest request
            ) {
        if(metricsService != null && !metricsExcludedUsers.contains(user.getUsername())){
            metricsService.recordView(file, request.getRemoteAddr());
        }
        return postProcessLinkDocument(documentRepository.read(file));
            }

    @CrossOrigin
    @SneakyThrows
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @GetMapping("documents/{file}.xml")
    public String readMetadataXml(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file
            ) {
        return "forward:/documents/" + file + "?format=" + GEMINI_XML_SHORT;
            }

    @ResponseBody
    @SneakyThrows
    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @GetMapping(value = "documents/{file}", produces = LINKED_JSON_VALUE)
    public MetadataDocument readLinkDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file
            ) {
        return documentRepository.read(file);
            }


    @ResponseBody
    @SneakyThrows
    @PreAuthorize("@permission.toAccess(#user, #file, #revision, 'VIEW')")
    @GetMapping(value = "history/{revision}/{file}")
    public MetadataDocument readMetadata(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file,
            @PathVariable("revision") String revision
            ) {
        return postProcessLinkDocument(documentRepository.read(file, revision));
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
    @SneakyThrows
    public DataRevision<CatalogueUser> deleteDocument(
            @ActiveUser CatalogueUser user,
            @PathVariable("file") String file
            ) {
        return documentRepository.delete(user, file);
            }
}
