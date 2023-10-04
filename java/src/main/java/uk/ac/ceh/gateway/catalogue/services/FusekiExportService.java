package uk.ac.ceh.gateway.catalogue.services;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.document.UnknownContentTypeException;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.exports.CatalogueExportService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RDF_TTL;

@Profile("server:eidc")
@Slf4j
@Service
@ToString
public class FusekiExportService implements CatalogueExportService {
    private final DocumentRepository documentRepository;
    private final DocumentWritingService documentWritingService;
    private final DataRepository<CatalogueUser> repo;
    private final MetadataListingService listing;

    // private final List<String> resourceTypes = List.of(
    //         "application",
    //         "dataset",
    //         "nonGeographicDataset",
    //         "service"
    // );

    public FusekiExportService(
            DocumentRepository documentRepository,
            DocumentWritingService documentWritingService,
            DataRepository<CatalogueUser> repo,
            MetadataListingService listing) {
        this.documentRepository = documentRepository;
        this.documentWritingService = documentWritingService;
        this.repo = repo;
        this.listing = listing;
    }

//    @Scheduled(cron = "0 0 3 * *")
//    @Scheduled(cron = "*/1 * * * *")
    @Scheduled(initialDelay = 5000, fixedDelay = 200000)
    @Override
    @SneakyThrows
    public void runExport(){
        log.info(listing.toString());
//        List<String> ids = listing.getPublicDocumentsOfCatalogue("eidc");
//        log.info(listing.getPublicDocumentsOfCatalogue("eidc").toString());
//        List<MetadataDocument> docs = listing.getPublicDocumentsOfCatalogue("eidc")
//                .stream()

        List<String> ids = listing.getPublicDocumentsOfCatalogue("eidc");
        log.info("hi");
        log.info(ids.toString());

        List<String> docs = ids.stream().map(id -> {
                    String toReturn = "empty";
                    try {
                        log.info(id);
                        MetadataDocument doc = documentRepository.read(id);
//                        log.info(doc.toString());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        documentWritingService.write(doc, RDF_TTL, baos);
                        toReturn =  baos.toString(StandardCharsets.UTF_8);
//                        return doc;
                    } catch (DocumentRepositoryException ex){
                        log.info("DocumentRepositoryException");
                        log.info(ex.getMessage());
//                        throw new RuntimeException(ex);
                    } catch (IOException ex){
                        log.info("IOException");
                        log.info(ex.getMessage());
//                        throw new RuntimeException(ex);
                    } catch (UnknownContentTypeException ex){
                        log.info("UnknownContentTypeException");
                        log.info(id);
                    }
                    log.info(toReturn);
                    return toReturn;
                })
                .collect(Collectors.toList());
        log.info("hi 2");
        log.info(docs.toString());

//        documentRepository.read(file);
//
//        log.info(ids.toString());
    }
}
