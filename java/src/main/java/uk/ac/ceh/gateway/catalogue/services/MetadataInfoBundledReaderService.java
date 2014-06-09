package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import lombok.Data;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.gemini.MetadataInfo;

/**
 *
 * @author cjohn
 */
@Data
public class MetadataInfoBundledReaderService<D> implements BundledReaderService<D> {
    private final DataRepository<?> repo;
    private final DocumentReadingService<D> documentReader;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final DocumentBundleService<D, MetadataInfo> documentBundler;
    
    @Override
    public D readBundle(String file, String revision) throws DataRepositoryException, IOException, UnknownContentTypeException {
        MetadataInfo documentInfo = documentInfoMapper.readInfo(
                                        repo.getData(revision, file + ".meta")
                                            .getInputStream());
        
        DataDocument dataDoc = repo.getData(revision, file + ".raw");
        D document = documentReader.read(dataDoc.getInputStream(),
                                                    documentInfo.getRawMediaType());
        return documentBundler.bundle(document, documentInfo);
    }
}
