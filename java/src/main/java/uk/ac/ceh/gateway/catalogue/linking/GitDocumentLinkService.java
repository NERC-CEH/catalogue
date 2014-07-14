package uk.ac.ceh.gateway.catalogue.linking;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Splitter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.elements.Link;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;

@Service
@Slf4j
public class GitDocumentLinkService implements DocumentLinkService {
    private final DataRepository<CatalogueUser> repo;
    private final BundledReaderService<GeminiDocument> documentBundleReader;
    private final LinkDatabase linkDatabase;

    @Autowired
    public GitDocumentLinkService(DataRepository<CatalogueUser> repo, 
                                  BundledReaderService<GeminiDocument> documentBundleReader,
                                  LinkDatabase linkingRepository) {
        
        this.repo = checkNotNull(repo);
        this.documentBundleReader = checkNotNull(documentBundleReader);
        this.linkDatabase = checkNotNull(linkingRepository);
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
                GeminiDocument document = documentBundleReader.readBundle(fileIdentifier, latestRev.getRevisionID());
                metadata.add(new Metadata(document));
                document.getCoupleResources().forEach((coupleResource) -> {
                    coupledResources.add(
                        CoupledResource.builder()
                            .fileIdentifier(document.getId())
                            .resourceIdentifier(coupleResource)
                            .build());
                });
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
    public Set<Link> getLinks(GeminiDocument document, UriComponentsBuilder builder) {
        if (document.getResourceType() != null) {
            switch (document.getResourceType().getValue().toLowerCase()) {
                case "dataset":
                    return createLinks(linkDatabase.findServicesForDataset(document.getId()), builder);

                case "service":
                    return createLinks(linkDatabase.findDatasetsForService(document.getId()), builder);
            }
        }
        return Collections.EMPTY_SET;
    }
    
    private Set<Link> createLinks(List<Metadata> metadata, UriComponentsBuilder builder) {
        final Set<Link> toReturn = new HashSet<>();
        metadata.forEach((meta) -> {
            toReturn.add(Link.builder()
                .title(meta.getTitle())
                .href(builder.buildAndExpand(meta.getFileIdentifier()).toUriString())
                .build());
        });
        return toReturn;
    }
    
    private Set<String> removeDuplicates(List<String> filenames) {
        Set<String> toReturn = new HashSet<>();
        filenames.forEach((filename) -> {
            toReturn.add(stripFileExtension(filename));
        });
        return toReturn;
    }
    
    private String stripFileExtension(String filename) {
        Iterable<String> split = Splitter.on(".").trimResults().omitEmptyStrings().split(filename);
        return split.iterator().next();
    }
}