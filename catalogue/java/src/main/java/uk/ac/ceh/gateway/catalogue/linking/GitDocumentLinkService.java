package uk.ac.ceh.gateway.catalogue.linking;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.Link;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

@Data
@Slf4j
public class GitDocumentLinkService implements DocumentLinkService {
    private final DataRepository<CatalogueUser> repo;
    private final BundledReaderService<MetadataDocument> documentBundleReader;
    private final DocumentListingService listingService;
    private final LinkDatabase linkDatabase;

    @Override
    public boolean isEmpty() {
        return linkDatabase.isEmpty();
    }
    
    @Override
    public void rebuildLinks() throws DocumentLinkingException {
        try {
            linkDatabase.empty();
            String revision = repo.getLatestRevision().getRevisionID();
            linkDocuments(listingService.filterFilenames(repo.getFiles(revision)));
        } catch (DataRepositoryException ex) {
            throw new DocumentLinkingException("Unable to get file names from Git", ex);
        }
    }

    @Override
    public void linkDocuments(List<String> fileIdentifiers) throws DocumentLinkingException {
        DataRevision<CatalogueUser> latestRev;
        Set<Metadata> metadata = new HashSet();
        Set<CoupledResource> coupledResources = new HashSet<>();
        
        try {
            latestRev = repo.getLatestRevision();
        } catch (DataRepositoryException ex) {
            throw new DocumentLinkingException("Unable to get latest revision from Git", ex);
        }
        
        DocumentLinkingException linkingException = new DocumentLinkingException("Errors from document linking");
        
        fileIdentifiers.forEach((fileIdentifier) -> {
            log.debug("linking with fileIdentifier: {}", fileIdentifier);
            try {
                MetadataDocument document = documentBundleReader.readBundle(fileIdentifier, latestRev.getRevisionID());
                if(document instanceof GeminiDocument) {
                    GeminiDocument geminiDocument = (GeminiDocument)document;
                    if (Optional.ofNullable(geminiDocument.getId()).isPresent()) {
                        metadata.add(new Metadata(geminiDocument));
                        geminiDocument.getCoupledResources().forEach((coupleResource) -> {
                            coupledResources.add(
                                CoupledResource.builder()
                                    .fileIdentifier(document.getId())
                                    .resourceIdentifier(coupleResource)
                                    .build());
                        });
                    }
                }
            } catch (Exception ex) {
                linkingException.addSuppressed(ex);
                log.error("Suppressed linking errors", (Object[]) linkingException.getSuppressed());
            }
        });
        
        linkDatabase.addMetadata(metadata);
        linkDatabase.addCoupledResources(coupledResources);
        
        if(linkingException.getSuppressed().length != 0) {
                throw linkingException;
        }
    }

    @Override
    public Set<Link> getLinks(GeminiDocument document, String urlFragment) {
        return Optional.ofNullable(document)
            .map(GeminiDocument::getResourceType)
            .map(Keyword::getValue)
            .map(r -> {
                List<Metadata> metadata;
                String associationType;
                switch (r.toLowerCase()) {
                    case "dataset":
                        metadata = linkDatabase.findServicesForDataset(document.getId());
                        associationType = "service";
                        break;
                    case "service":
                        metadata = linkDatabase.findDatasetsForService(document.getId());
                        associationType = "dataset";
                        break;
                    default:
                        metadata = Collections.emptyList();
                        associationType = "";
                }
                return createLinks(metadata, urlFragment, associationType);
            })
            .orElse(Collections.emptySet());
    }
    
    @Override
    public Optional<Link> getParent(GeminiDocument document, String urlFragment) {
        return linkDatabase.findParent(document.getId())
            .map(m -> createLink(m, urlFragment, "series"));
    }

    @Override
    public Set<Link> getChildren(GeminiDocument document, String urlFragment) {
        return createLinks(linkDatabase.findChildren(document.getId()), urlFragment, "isComposedOf");
    }
    
    @Override
    public Optional<Link> getRevised(GeminiDocument document, String urlFragment) {
        return linkDatabase.findRevised(document.getId())
            .map(m -> createLink(m, urlFragment, "revised"));
    }
    
    @Override
    public Optional<Link> getRevisionOf(GeminiDocument document, String urlFragment) {
        return linkDatabase.findRevisionOf(document.getId())
            .map(m -> createLink(m, urlFragment, "revisionOf"));
    }
    
    private Set<Link> createLinks(List<Metadata> metadata, String urlFragment, String associationType) {
        return metadata.stream()
            .filter(Objects::nonNull)
            .map(m -> createLink(m, urlFragment, associationType) )
            .collect(Collectors.toSet());
    }
    
    private Link createLink(Metadata metadata, String urlFragment, String associationType) {
        return Link.builder()
            .title(metadata.getTitle())
            .href(UriComponentsBuilder.fromHttpUrl(urlFragment).path(metadata.getFileIdentifier()).build().toUriString())
            .associationType(associationType)
            .build();
    }
}