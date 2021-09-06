package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.model.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@ToString
@Service
public class ServiceAgreementService {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final DocumentTypeLookupService documentTypeLookupService;
    private static final String FOLDER = "service-agreements/";

    public ServiceAgreementService(
            DocumentTypeLookupService documentTypeLookupService,
            DataRepository<CatalogueUser> repo,
            DocumentInfoMapper<MetadataInfo> documentInfoMapper
    ) {
        this.documentTypeLookupService = documentTypeLookupService;
        this.repo = repo;
        this.documentInfoMapper = documentInfoMapper;
        log.info("Creating {}", this);
    }


    @SneakyThrows
    public boolean metadataRecordExists(String id) {
        try {
            repo.getData(id);
        } catch (DataRepositoryException e) {
            throw e;
        }
        return true;
    }

    @SneakyThrows
    public DataDocument get(String id) {
        try {
            return repo.getData(FOLDER + id);
        } catch (DataRepositoryException e) {
            throw e;
        }
    }


    @SneakyThrows
    public void save(CatalogueUser user, String id, String catalogue, AbstractMetadataDocument serviceAgreementDocument) {

        MetadataInfo metadataInfo = createMetadataInfoWithDefaultPermissions(serviceAgreementDocument, user, MediaType.APPLICATION_JSON, catalogue);

        Path tmpFile = Files.createTempFile("upload", null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(serviceAgreementDocument);

        oos.flush();
        oos.close();

        InputStream is = new ByteArrayInputStream(baos.toByteArray());

        Files.copy(is, tmpFile, StandardCopyOption.REPLACE_EXISTING);

        repo.submitData(String.format("%s.meta", FOLDER + id), (o) -> documentInfoMapper.writeInfo(metadataInfo, o))
                .submitData(String.format("%s.raw", FOLDER + id), (o) -> Files.copy(tmpFile, o))
                .commit(user, catalogue);
    }

    @SneakyThrows
    public DataRevision<CatalogueUser> delete(CatalogueUser user, String id) {
        return repo.deleteData(FOLDER + id + ".meta")
                .deleteData(FOLDER + id + ".raw")
                .commit(user, String.format("delete document: %s", id));
    }

    private MetadataInfo createMetadataInfoWithDefaultPermissions(MetadataDocument document, CatalogueUser user, MediaType mediaType, String catalogue) {
        MetadataInfo toReturn = MetadataInfo.builder()
                .rawType(mediaType.toString())
                .documentType(documentTypeLookupService.getName(document.getClass()))
                .catalogue(catalogue)
                .build();
        String username = user.getUsername();
        toReturn.addPermission(Permission.VIEW, username);
        toReturn.addPermission(Permission.EDIT, username);
        toReturn.addPermission(Permission.DELETE, username);
        return toReturn;
    }

}
