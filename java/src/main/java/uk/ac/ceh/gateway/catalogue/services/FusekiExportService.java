package uk.ac.ceh.gateway.catalogue.services;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.exports.CatalogueExportService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.util.*;
import java.util.stream.Collectors;

@Profile({"server:eidc","server:elter"})
@Slf4j
@Service
@ToString
public class FusekiExportService implements CatalogueExportService {
    private final CatalogueService catalogueService;
    private final DocumentRepository documentRepository;
    private final DataRepository<CatalogueUser> repo;
    private final MetadataListingService listing;
    private final Configuration configuration;
    private final Catalogue catalogue;
    private final RestTemplate restTemplate;
    private final String baseUri;

    public FusekiExportService(
            CatalogueService catalogueService,
            DocumentRepository documentRepository,
            DataRepository<CatalogueUser> repo,
            MetadataListingService listing,
            Configuration configuration,
            @Qualifier("normal") RestTemplate restTemplate,
            @Value("${documents.baseUri}") String baseUri) {
        this.catalogueService = catalogueService;
        this.documentRepository = documentRepository;
        this.repo = repo;
        this.listing = listing;
        this.configuration = configuration;
        this.restTemplate = restTemplate;
        catalogue = catalogueService.defaultCatalogue();
        this.baseUri = baseUri;
    }

    //    @Scheduled(cron = "0 0 3 * *")
    //    @Scheduled(cron = "*/1 * * * *")
    @Scheduled(initialDelay = 5000, fixedDelay = 200000)
    @Override
    @SneakyThrows
    public void runExport() {
        String bigTtl = getBigTtl();
        log.info(bigTtl);
    }

    private String getBigTtl() {
        List<String> ids = getRequiredIds();
        String catalogueTtl = getCatalogueTtl(getCatalogueModel(ids));
        List<String> recordsTtl = getRecordsTtl(ids);
        return catalogueTtl.concat(String.join("\n", recordsTtl));
    }

    private List<String> getRequiredIds(){
        List<String> ids = listing.getPublicDocumentsOfCatalogue(catalogue.getId());
        return ids.stream()
                .map(this::getMetadataDocument)
                .filter(this::isRequired)
                .map(MetadataDocument::getId)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public String getCatalogueTtl(Map<String, Object> model){
        val freemarkerTemplate = configuration.getTemplate("rdf/catalogue.ttl.ftlh");
        return FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, model);
    }

    private Map<String, Object> getCatalogueModel(List<String> ids){
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("records", ids);
        model.put("catalogue", catalogue.getId());
        model.put("title", catalogue.getTitle());
        model.put("uri", baseUri);
        return model;
    }

    private List<String> getRecordsTtl(List<String> ids){
        return ids.stream()
                .map(this::getMetadataDocument)
                .map(this::docToString)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private MetadataDocument getMetadataDocument(String id) {
        return documentRepository.read(id);
    }

    private boolean isRequired(MetadataDocument doc) {
        String[] requiredTypes = {"service","dataset"};
        return Arrays.asList(requiredTypes).contains(doc.getType());
    }

    @SneakyThrows
    public String docToString(MetadataDocument model){
        val freemarkerTemplate = configuration.getTemplate("rdf/ttlUnprefixed.ftlh");
        return FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, model);
    }

}
