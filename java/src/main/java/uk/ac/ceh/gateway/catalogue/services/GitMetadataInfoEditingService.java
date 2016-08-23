package uk.ac.ceh.gateway.catalogue.services;

import java.io.IOException;
import static java.lang.String.format;
import java.net.URI;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DocumentDoesNotExistException;
import uk.ac.ceh.gateway.catalogue.model.DocumentSaveException;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import static java.lang.String.format;

@Service
public class GitMetadataInfoEditingService implements MetadataInfoEditingService {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentReadingService documentReader;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final DocumentTypeLookupService representationService;
    private static final String META_SUFFIX = "%s.meta";
    private static final String RAW_SUFFIX = "%s.raw";

    @Autowired
    public GitMetadataInfoEditingService(DataRepository<CatalogueUser> repo, DocumentReadingService documentReader, DocumentInfoMapper<MetadataInfo> documentInfoMapper, DocumentTypeLookupService representationService) {
        this.repo = repo;
        this.documentReader = documentReader;
        this.documentInfoMapper = documentInfoMapper;
        this.representationService = representationService;
    }

    @Override
    public MetadataDocument getMetadataDocument(@NonNull String fileIdentifier, @NonNull URI metadataUri) {
        try {
            MetadataInfo documentInfo = documentInfoMapper.readInfo(
                repo.getData(format(META_SUFFIX, fileIdentifier)).getInputStream()
            );
            
            DataDocument dataDoc = repo.getData(format(RAW_SUFFIX, fileIdentifier));
            MetadataDocument document = documentReader.read(dataDoc.getInputStream(),
                documentInfo.getRawMediaType(),
                representationService.getType(documentInfo.getDocumentType()));
            document.attachMetadata(documentInfo);
            document.attachUri(metadataUri);
            return document;
        } catch (IOException | UnknownContentTypeException ex) {
            String message = format("Cannot find document for: %s", fileIdentifier);
            throw new DocumentDoesNotExistException(message, ex);
        }
    }

    @Override
    public void saveMetadataInfo(@NonNull String fileIdentifier, @NonNull MetadataInfo info, @NonNull CatalogueUser user, @NonNull String commitMessage) {
        try {
            repo.submitData(format(META_SUFFIX, fileIdentifier), (o)-> documentInfoMapper.writeInfo(info, o))
                .commit(user, commitMessage);
        } catch (DataRepositoryException ex) {
            String message = format("%s was trying to save: %s for: %s with commit message: %s", user, info, fileIdentifier, commitMessage);
            throw new DocumentSaveException(message, ex);
        }
    }
}