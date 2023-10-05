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
import uk.ac.ceh.gateway.catalogue.exports.CatalogueExportService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.util.*;
import java.util.stream.Collectors;


@Profile("server:eidc")
@Slf4j
@Service
@ToString
public class FusekiExportService implements CatalogueExportService {
    private static final String CATALOGUE_ID = "eidc";

    private final CatalogueService catalogueService;
    private final DocumentRepository documentRepository;
    private final DataRepository<CatalogueUser> repo;
    private final MetadataListingService listing;
    private final Configuration configuration;
    private final Catalogue catalogue;

    public FusekiExportService(
            CatalogueService catalogueService,
            DocumentRepository documentRepository,
            DataRepository<CatalogueUser> repo,
            MetadataListingService listing, Configuration configuration) {
        this.catalogueService = catalogueService;
        this.documentRepository = documentRepository;
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
        List<String> ids = getRequiredIds();
        String catalogueTtl = getCatalogueTtl(getCatalogueModel(ids));
        List<String> recordsTtl = getRecordsTtl(ids);
        String bigTtl = catalogueTtl.concat(String.join("\n", recordsTtl));
        log.info(bigTtl);
    }

    private List<String> getRequiredIds(){
        List<String> ids = listing.getPublicDocumentsOfCatalogue(CATALOGUE_ID);
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
        model.put("catalogue", CATALOGUE_ID);
        model.put("title", catalogue.getTitle());
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
