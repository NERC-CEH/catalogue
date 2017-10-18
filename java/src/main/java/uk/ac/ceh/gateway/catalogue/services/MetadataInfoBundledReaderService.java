package uk.ac.ceh.gateway.catalogue.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;

import java.io.IOException;

@Service
@AllArgsConstructor
public class MetadataInfoBundledReaderService implements BundledReaderService<MetadataDocument> {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentReadingService documentReader;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final DocumentTypeLookupService representationService;
    private final PostProcessingService postProcessingService;
    private final DocumentIdentifierService documentIdentifierService;
    
    @Override
    public MetadataDocument readBundle(String file) throws DataRepositoryException, IOException, PostProcessingException {
        return readBundle(file, repo.getLatestRevision().getRevisionID(), false);
    }
    
    @Override
    public MetadataDocument readBundle(String file, String revision) throws DataRepositoryException, IOException, PostProcessingException, UnknownContentTypeException {
        return readBundle(file, revision, true);
    }
    
    private MetadataDocument readBundle(String file, String revision, boolean history) throws DataRepositoryException, IOException, PostProcessingException, UnknownContentTypeException {
        MetadataInfo documentInfo = documentInfoMapper.readInfo(
                                        repo.getData(revision, file + ".meta")
                                            .getInputStream());

        DataDocument dataDoc = repo.getData(revision, file + ".raw");
        MetadataDocument document = documentReader.read(dataDoc.getInputStream(),
                                        documentInfo.getRawMediaType(),
                                        representationService.getType(documentInfo.getDocumentType()));
        document.setMetadata(documentInfo.withRawType(null));
        
        if (history) {
            document.setUri(documentIdentifierService.generateUri(file, revision));
        } else {
            document.setUri(documentIdentifierService.generateUri(file));
        }
        
        postProcessingService.postProcess(document);
        return document;
    }
}
