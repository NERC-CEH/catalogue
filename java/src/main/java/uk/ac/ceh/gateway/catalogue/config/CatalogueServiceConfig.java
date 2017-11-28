package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;
import uk.ac.ceh.gateway.catalogue.services.InMemoryCatalogueService;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;

@Configuration
public class CatalogueServiceConfig {
    public static final String GEMINI_DOCUMENT = "GEMINI_DOCUMENT";
    public static final String EF_DOCUMENT = "EF_DOCUMENT";
    public static final String IMP_DOCUMENT = "IMP_DOCUMENT";
    public static final String CEH_MODEL = "CEH_MODEL";
    public static final String CEH_MODEL_APPLICATION = "CEH_MODEL_APPLICATION";
    public static final String LINK_DOCUMENT = "LINK_DOCUMENT";

    @Bean
    public CatalogueService catalogueService() {
        String defaultCatalogueKey = "eidc";

        Catalogue.DocumentType gemini = Catalogue.DocumentType.builder()
            .title("Data Resource")
            .type(GEMINI_DOCUMENT)
            .build();

        Catalogue.DocumentType ef = Catalogue.DocumentType.builder()
            .title("Monitoring")
            .type(EF_DOCUMENT)
            .build();

        Catalogue.DocumentType imp = Catalogue.DocumentType.builder()
            .title("Model")
            .type(IMP_DOCUMENT)
            .build();

        Catalogue.DocumentType cehModel = Catalogue.DocumentType.builder()
            .title("Model")
            .type(CEH_MODEL)
            .build();

        Catalogue.DocumentType cehModelApplication = Catalogue.DocumentType.builder()
            .title("Model Application")
            .type(CEH_MODEL_APPLICATION)
            .build();

        Catalogue.DocumentType link = Catalogue.DocumentType.builder()
            .title("Link")
            .type(LINK_DOCUMENT)
            .build();

        Catalogue.DocumentType agent = Catalogue.DocumentType.builder()
            .title("Agent")
            .type(OSDP_AGENT_SHORT)
            .build();

        Catalogue.DocumentType dataset = Catalogue.DocumentType.builder()
            .title("Dataset")
            .type(OSDP_DATASET_SHORT)
            .build();

        Catalogue.DocumentType model = Catalogue.DocumentType.builder()
            .title("Model")
            .type(OSDP_MODEL_SHORT)
            .build();

        Catalogue.DocumentType monitoringActivity = Catalogue.DocumentType.builder()
            .title("Monitoring Activity")
            .type(OSDP_MONITORING_ACTIVITY_SHORT)
            .build();

        Catalogue.DocumentType monitoringFacility = Catalogue.DocumentType.builder()
            .title("Monitoring Facility")
            .type(OSDP_MONITORING_FACILITY_SHORT)
            .build();

        Catalogue.DocumentType monitoringProgramme = Catalogue.DocumentType.builder()
            .title("Monitoring Programme")
            .type(OSDP_MONITORING_PROGRAMME_SHORT)
            .build();

        Catalogue.DocumentType publication = Catalogue.DocumentType.builder()
            .title("Publication")
            .type(OSDP_PUBLICATION_SHORT)
            .build();

        Catalogue.DocumentType sample = Catalogue.DocumentType.builder()
            .title("Sample")
            .type(OSDP_SAMPLE_SHORT)
            .build();

        return new InMemoryCatalogueService(
            defaultCatalogueKey,

            Catalogue.builder()
                .id("osdp")
                .title("Open Soils Data Platform")
                .url("http://www.ceh.ac.uk")
                .documentType(agent)
                .documentType(dataset)
                .documentType(model)
                .documentType(monitoringActivity)
                .documentType(monitoringFacility)
                .documentType(monitoringProgramme)
                .documentType(publication)
                .documentType(sample)
                .facetKey("resourceType")
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("m")
                .title("Modelling")
                .url("http://www.ceh.ac.uk")
                .facetKey("resourceType")
                .documentType(cehModel)
                .documentType(cehModelApplication)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("nc")
                .title("Natural Capital")
                .url("http://www.ceh.ac.uk")
                .facetKey("resourceType")
                .documentType(gemini)
                .documentType(link)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("inms")
                .title("International Nitrogen Management System")
                .url("http://www.inms.international/")
                .facetKey("resourceType")
                .facetKey("impScale")
                .facetKey("impTopic")
                .facetKey("inmsPollutant")
                .facetKey("modelType")
                .facetKey("inmsDemonstrationRegion")
                .documentType(gemini)
                .documentType(cehModel)
                .documentType(cehModelApplication)
                .documentType(link)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("edge")
                .title("EDgE")
                .url("https://edge.climate.copernicus.eu")
                .facetKey("resourceType")
                .documentType(gemini)
                .documentType(link)
                .fileUpload(true)
                .build(),

            Catalogue.builder()
                .id("ceh")
                .title("Centre for Ecology & Hydrology")
                .url("https://eip.ceh.ac.uk")
                .facetKey("topic")
                .facetKey("resourceType")
                .facetKey("licence")
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id(defaultCatalogueKey)
                .title("Environmental Information Data Centre")
                .url("http://eidc.ceh.ac.uk")
                .facetKey("topic")
                .facetKey("resourceType")
                .facetKey("licence")
                .documentType(gemini)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("cmp")
                .title("Catchment Management Modelling Platform")
                .url("http://www.cammp.org.uk/index.php")
                .facetKey("resourceType")
                .facetKey("impCaMMPIssues")
                .facetKey("impDataType")
                .facetKey("impScale")
                .facetKey("impTopic")
                .facetKey("impWaterPollutant")
                .documentType(gemini)
                .documentType(imp)
                .documentType(link)
                .fileUpload(true)
                .build(),

            Catalogue.builder()
                .id("assist")
                .title("Achieving Sustainable Agricultural Systems")
                .url("http://www.ceh.ac.uk/ASSIST")
                .facetKey("resourceType")
                .facetKey("licence")
                .documentType(gemini)
                .documentType(link)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("inlicensed")
                .title("CEH In-licensed Datasets")
                .url("http://intranet.ceh.ac.uk/procedures/commercialisation/data-licensing-ipr/in-licensed-data-list")
                .facetKey("resourceType")
                .documentType(gemini)
                .fileUpload(false)
                .build()
        );
    }
}
