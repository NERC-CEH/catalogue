package uk.ac.ceh.gateway.catalogue.linking;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

public class DatabaseDocumentLinkingService implements DocumentLinkingService {
    private final DataRepository<CatalogueUser> repo;
    private final BundledReaderService<GeminiDocument> documentBundleReader;
    private final LinkingRepository linkingRepository;

    @Autowired
    public DatabaseDocumentLinkingService(DataRepository<CatalogueUser> repo, 
                                          BundledReaderService<GeminiDocument> documentBundleReader,
                                          LinkingRepository linkingRepository) {
        this.repo = checkNotNull(repo);
        this.documentBundleReader = checkNotNull(documentBundleReader);
        this.linkingRepository = checkNotNull(linkingRepository);
    }    

    @Override
    public void rebuildLinks() throws DocumentLinkingException {
        try {
            linkDocuments(repo.getFiles());
        } catch (DataRepositoryException ex) {
            throw new DocumentLinkingException("Unable to get file names from Git", ex);
        }
    }

    @Override
    public void linkDocuments(List<String> fileIdentifiers) throws DocumentLinkingException {
        DataRevision<CatalogueUser> latestRev;
        
        try {
            latestRev = repo.getLatestRevision();
        } catch (DataRepositoryException ex) {
            throw new DocumentLinkingException("Unable to get latest revision from Git", ex);
        }
        
        for (String fileIdentifier : fileIdentifiers) {
            try {
                link(documentBundleReader.readBundle(fileIdentifier, latestRev.getRevisionID()));
            } catch (DataRepositoryException ex) {
                throw new DocumentLinkingException("Problem retrieving GeminiDocument", ex);
            } catch (IOException | UnknownContentTypeException ex) {
                throw new DocumentLinkingException("Problem retrieving GeminiDocument", ex);
            }
        }
    }

    private void link(GeminiDocument document) throws DocumentLinkingException {
        linkingRepository.delete(document);
        linkingRepository.add(document);
    }
}