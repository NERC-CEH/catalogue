package uk.ac.ceh.gateway.catalogue.catalogue;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.controllers.IfMatchRevision;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("documents")
public class CatalogueDocumentController {
    private final DocumentRepository documentRepository;
    private final CatalogueService catalogueService;
    private final CachedDataRepository cachedDataRepository;

    public CatalogueDocumentController(
        DocumentRepository documentRepository,
        CatalogueService catalogueService,
        CachedDataRepository cachedDataRepository
    ) {
        this.documentRepository = documentRepository;
        this.catalogueService = catalogueService;
        this.cachedDataRepository = cachedDataRepository;
        log.info("Creating");
    }

    @SneakyThrows
    @PreAuthorize("@permission.userCanView(#file)")
    @GetMapping("{file}/catalogue")
    public ResponseEntity<CatalogueResource> currentCatalogue (
        @PathVariable String file
    ) throws DocumentRepositoryException {
        CatalogueResource resource = new CatalogueResource(documentRepository.read(file));
        String revision = cachedDataRepository.getDocumentRevisionId(file + ".meta");
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (revision != null) {
            builder.eTag(revision); // Spring quotes this into a strong ETag: "revision"
        }
        return builder.body(resource);
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @PutMapping("{file}/catalogue")
    public CatalogueResource updateCatalogue (
        @ActiveUser CatalogueUser user,
        @PathVariable String file,
        @RequestBody CatalogueResource catalogueResource,
        @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
    ) throws DocumentRepositoryException {
        String expectedRevision = IfMatchRevision.require(ifMatch);
        val document = documentRepository.read(file);
        val metadata = document.getMetadata();
        document.setMetadata(
            metadata.withCatalogue(catalogueResource.getValue())
        );
        log.debug(document.toString());
        val newDocument =  documentRepository.save(
            user,
            document,
            file,
            String.format("Catalogues of %s changed.", file),
            expectedRevision
        );
        log.debug(newDocument.toString());
        return new CatalogueResource(newDocument);
    }

    @SneakyThrows
    @PreAuthorize("@permission.userCanView(#file)")
    @GetMapping("{file}/catalogue-view")
    public ResponseEntity<CatalogueViewResource> currentCatalogueView(
        @PathVariable String file
    ) throws DocumentRepositoryException {
        CatalogueViewResource resource = new CatalogueViewResource(documentRepository.read(file));
        String revision = cachedDataRepository.getDocumentRevisionId(file + ".meta");
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (revision != null) {
            builder.eTag(revision); // Spring quotes this into a strong ETag: "revision"
        }
        return builder.body(resource);
    }

    @PreAuthorize("@permission.userCanEdit(#file)")
    @PutMapping("{file}/catalogue-view")
    public CatalogueViewResource updateCatalogueView(
        @ActiveUser CatalogueUser user,
        @PathVariable String file,
        @RequestBody CatalogueViewResource resource,
        @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
    ) throws DocumentRepositoryException {
        String expectedRevision = IfMatchRevision.require(ifMatch);
        val document = documentRepository.read(file);
        val primaryCatalogue = document.getCatalogue();
        Set<String> knownIds = catalogueService.retrieveAll().stream()
            .map(Catalogue::getId)
            .collect(Collectors.toSet());
        List<String> filtered = resource.getValue().stream()
            .filter(knownIds::contains)
            .filter(id -> !id.equals(primaryCatalogue))
            .distinct()
            .collect(Collectors.toList());
        document.setMetadata(document.getMetadata().withCatalogueView(filtered));
        val newDocument = documentRepository.save(
            user,
            document,
            file,
            String.format("Secondary catalogues of %s changed.", file),
            expectedRevision
        );
        return new CatalogueViewResource(newDocument);
    }
}
