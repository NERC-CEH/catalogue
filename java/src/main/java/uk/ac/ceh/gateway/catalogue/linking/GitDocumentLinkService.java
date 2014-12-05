package uk.ac.ceh.gateway.catalogue.linking;

import com.google.common.base.Splitter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Link;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;

@Data
@Slf4j
public class GitDocumentLinkService implements DocumentLinkService {
    private final DataRepository<CatalogueUser> repo;
    private final BundledReaderService<MetadataDocument> documentBundleReader;
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
            linkDocuments(removeDuplicates(repo.getFiles(revision)));
        } catch (DataRepositoryException ex) {
            throw new DocumentLinkingException("Unable to get file names from Git", ex);
        }
    }

    @Override
    public void linkDocuments(Set<String> fileIdentifiers) throws DocumentLinkingException {
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
                    metadata.add(new Metadata(geminiDocument));
                    geminiDocument.getCoupledResources().forEach((coupleResource) -> {
                        coupledResources.add(
                            CoupledResource.builder()
                                .fileIdentifier(document.getId())
                                .resourceIdentifier(coupleResource)
                                .build());
                    });
                }
            } catch (Exception ex) {
                linkingException.addSuppressed(ex);
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
        if (document.getResourceType() != null) {
            switch (document.getResourceType().toLowerCase()) {
                case "dataset":
                    return createLinks(linkDatabase.findServicesForDataset(document.getId()), urlFragment, "service");

                case "service":
                    return createLinks(linkDatabase.findDatasetsForService(document.getId()), urlFragment, "dataset");
            }
        }
        return Collections.EMPTY_SET;
    }
    
    @Override
    public Link getParent(GeminiDocument document, String urlFragment) {
        return createLinks(Arrays.asList(linkDatabase.findParent(document.getId())), urlFragment, "series")
            .stream()
            .findFirst()
            .orElse(null);
    }

    @Override
    public Set<Link> getChildren(GeminiDocument document, String urlFragment) {
        return createLinks(linkDatabase.findChildren(document.getId()), urlFragment, "isComposedOf");
    }
    
    @Override
    public Link getRevised(GeminiDocument document, String urlFragment) {
        return createLinks(Arrays.asList(linkDatabase.findRevised(document.getId())), urlFragment, "revised")
            .stream()
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public Link getRevisionOf(GeminiDocument document, String urlFragment) {
        return createLinks(Arrays.asList(linkDatabase.findRevisionOf(document.getId())), urlFragment, "revisionOf")
            .stream()
            .findFirst()
            .orElse(null);
    }
    
    private Set<Link> createLinks(List<Metadata> metadata, String urlFragment, String associationType) {
        return metadata.stream()
            .filter(m -> m != null)
            .map(m -> {
                return Link.builder()
                    .title(m.getTitle())
                    .href(UriComponentsBuilder.fromHttpUrl(urlFragment).path(m.getFileIdentifier()).build().toUriString())
                    .associationType(associationType)
                    .build();
            })
            .collect(Collectors.toSet());
    }
    
    private Set<String> removeDuplicates(List<String> filenames) {
        return filenames.stream()
            .map(filename -> stripFileExtension(filename))
            .distinct()
            .collect(Collectors.toSet());
    }
    
    private String stripFileExtension(String filename) {
        Iterable<String> split = Splitter.on(".").trimResults().omitEmptyStrings().split(filename);
        return split.iterator().next();
    }
}