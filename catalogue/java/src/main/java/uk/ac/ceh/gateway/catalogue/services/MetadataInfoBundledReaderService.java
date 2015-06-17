package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import lombok.Data;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

/**
 *
 * @author cjohn
 */
@Data
public class MetadataInfoBundledReaderService implements BundledReaderService<MetadataDocument> {
    private final DataRepository<?> repo;
    private final DocumentReadingService documentReader;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final DocumentTypeLookupService representationService;
    
    @Override
    public MetadataDocument readBundle(String file, String revision) throws DataRepositoryException, IOException, UnknownContentTypeException {
        MetadataInfo documentInfo = documentInfoMapper.readInfo(
                                        repo.getData(revision, file + ".meta")
                                            .getInputStream());

        DataDocument dataDoc = repo.getData(revision, file + ".raw");
        MetadataDocument document = documentReader.read(dataDoc.getInputStream(),
                                        documentInfo.getRawMediaType(),
                                        representationService.getType(documentInfo.getDocumentType()));
        document.attachMetadata(documentInfo);
        documentInfo.hideMediaType();
        return document;
    }
}
