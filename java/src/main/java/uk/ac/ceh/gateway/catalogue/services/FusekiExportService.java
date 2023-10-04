package uk.ac.ceh.gateway.catalogue.services;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.exports.CatalogueExportService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RDF_TTL;

@Profile("server:eidc")
@Slf4j
@Service
@ToString
public class FusekiExportService implements CatalogueExportService {
    private static final String CATALOGUE_ID = "eidc";

    private final CatalogueService catalogueService;
    private final DocumentRepository documentRepository;
    private final DocumentWritingService documentWritingService;
    private final DataRepository<CatalogueUser> repo;
    private final MetadataListingService listing;
    private final Configuration configuration;
    private final Catalogue catalogue;

    public FusekiExportService(
            CatalogueService catalogueService,
            DocumentRepository documentRepository,
            DocumentWritingService documentWritingService,
            DataRepository<CatalogueUser> repo,
            MetadataListingService listing, Configuration configuration) {
        this.catalogueService = catalogueService;
        this.documentRepository = documentRepository;
        this.documentWritingService = documentWritingService;
        this.repo = repo;
        this.listing = listing;
        this.configuration = configuration;
        catalogue = catalogueService.retrieve(CATALOGUE_ID);
    }

    //    @Scheduled(cron = "0 0 3 * *")
    //    @Scheduled(cron = "*/1 * * * *")
    @Scheduled(initialDelay = 5000, fixedDelay = 200000)
    @Override
    @SneakyThrows
    public void runExport() {
        String bigTtl = getBigTtl(getModel());
        log.info(bigTtl);
    }

    private Map<String, Object> getModel(){
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("records", getRecords());
        model.put("catalogue", CATALOGUE_ID);
        model.put("title", catalogue.getTitle());
        return model;
    }

    private List<String> getRecords(){
            List<String> ids = listing.getPublicDocumentsOfCatalogue(CATALOGUE_ID);
            return ids.stream()
                    .map(this::getMetadataDocument)
                    .filter(this::isRequired)
                    .map(this::docToString)
                    .collect(Collectors.toList());
    }

    @SneakyThrows
    private MetadataDocument getMetadataDocument(String id) {
        return documentRepository.read(id);
    }

    private boolean isRequired(MetadataDocument doc) {
        String[] requiredTypes = {"service","dataset"};
        return Arrays.stream(requiredTypes).anyMatch(doc.getType()::equals);
    }

    @SneakyThrows
    private String docToString(MetadataDocument doc){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        documentWritingService.write(doc, RDF_TTL, out);
        return out.toString(StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public String getBigTtl(Map<String, Object> model){
        val freemarkerTemplate = configuration.getTemplate("rdf/catalogue.ttl.ftlh");
        String processedTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, model);
        return processedTemplate;
    }

}