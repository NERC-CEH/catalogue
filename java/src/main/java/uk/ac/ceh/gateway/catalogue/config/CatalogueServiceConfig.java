package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.Catalogue.DocumentType;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;
import uk.ac.ceh.gateway.catalogue.services.InMemoryCatalogueService;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;

@Configuration
public class CatalogueServiceConfig {
    public static final String DEPOSIT_REQUEST_DOCUMENT = "DEPOSIT_REQUEST_DOCUMENT";
    public static final String GEMINI_DOCUMENT = "GEMINI_DOCUMENT";
    public static final String EF_DOCUMENT = "EF_DOCUMENT";
    public static final String IMP_DOCUMENT = "IMP_DOCUMENT";
    public static final String CEH_MODEL = "CEH_MODEL";
    public static final String CEH_MODEL_APPLICATION = "CEH_MODEL_APPLICATION";
    public static final String LINK_DOCUMENT = "LINK_DOCUMENT";

    @Bean
    public CatalogueService catalogueService() {
        String defaultCatalogueKey = "eidc";

        DocumentType gemini = DocumentType.builder()
            .title("Data resource")
            .type(GEMINI_DOCUMENT)
            .build();

        DocumentType imp = DocumentType.builder()
            .title("Model")
            .type(IMP_DOCUMENT)
            .build();

        DocumentType cehModel = DocumentType.builder()
            .title("Model")
            .type(CEH_MODEL)
            .build();

        DocumentType cehModelApplication = DocumentType.builder()
            .title("Model Application")
            .type(CEH_MODEL_APPLICATION)
            .build();

        DocumentType link = DocumentType.builder()
            .title("Link")
            .type(LINK_DOCUMENT)
            .build();

        DocumentType agent = DocumentType.builder()
            .title("Agent")
            .type(OSDP_AGENT_SHORT)
            .build();

        DocumentType dataset = DocumentType.builder()
            .title("Dataset")
            .type(OSDP_DATASET_SHORT)
            .build();

        DocumentType model = DocumentType.builder()
            .title("Model")
            .type(OSDP_MODEL_SHORT)
            .build();

        DocumentType monitoringActivity = DocumentType.builder()
            .title("Monitoring Activity")
            .type(OSDP_MONITORING_ACTIVITY_SHORT)
            .build();

        DocumentType monitoringFacility = DocumentType.builder()
            .title("Monitoring Facility")
            .type(OSDP_MONITORING_FACILITY_SHORT)
            .build();

        DocumentType monitoringProgramme = DocumentType.builder()
            .title("Monitoring Programme")
            .type(OSDP_MONITORING_PROGRAMME_SHORT)
            .build();

        DocumentType publication = DocumentType.builder()
            .title("Publication")
            .type(OSDP_PUBLICATION_SHORT)
            .build();

        DocumentType sample = DocumentType.builder()
            .title("Sample")
            .type(OSDP_SAMPLE_SHORT)
            .build();

        DocumentType sampleArchive = DocumentType.builder()
            .title("Sample Archive")
            .type(SAMPLE_ARCHIVE_SHORT)
            .build();

        DocumentType dataType = DocumentType.builder()
            .title("Data type")
            .type(DATA_TYPE_SHORT)
            .build();

        return new InMemoryCatalogueService(
            defaultCatalogueKey,

            Catalogue.builder()
                .id("sa")
                .title("Sample Archive")
                .url("")
                .documentType(sampleArchive)
                .fileUpload(false)
                .build(),

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
                .title("Model Management")
                .url("http://intranet.ceh.ac.uk/procedures/science-information-management/science-information-management-full-procedures/model-management-procedures")
                .facetKey("resourceType")
                .documentType(cehModel)
                .documentType(cehModelApplication)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("nc")
                .title("Natural Capital")
                .url("http://www.ceh.ac.uk")
                .facetKey("ncAssets")
                .facetKey("ncCaseStudy")
                .facetKey("ncDrivers")
                .facetKey("ncEcosystemServices")
                .facetKey("ncGeographicalScale")
                .documentType(gemini)
                .documentType(cehModel)
                .documentType(cehModelApplication)
                .documentType(link)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("inms")
                .title("International Nitrogen Management System")
                .url("http://www.inms.international/")
                .facetKey("recordType")
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
                .facetKey("recordType")
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
                .title("EIDC")
                .url("http://eidc.ceh.ac.uk")
                .facetKey("topic")
                .facetKey("recordType")
                .facetKey("status")
                .facetKey("licence")
                .documentType(gemini)
                .documentType(dataType)
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
                .url("http://assist.ceh.ac.uk")
                .facetKey("resourceType")
                .facetKey("licence")
                .documentType(gemini)
                .documentType(cehModel)
                .documentType(cehModelApplication)
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
                .build(),

            Catalogue.builder()
                .id("elter")
                .title("eLTER")
                .url("http://www.ceh.ac.uk")
                .facetKey("documentType")
                .documentType(DocumentType.builder().title("CompositeFeature").type("CompositeFeature").build())
                .documentType(DocumentType.builder().title("Condition").type("Condition").build())
                .documentType(DocumentType.builder().title("DeploymentRelatedProcessDuration").type("DeploymentRelatedProcessDuration").build())
                .documentType(DocumentType.builder().title("Input").type("Input").build())
                .documentType(DocumentType.builder().title("Manufacturer").type("Manufacturer").build())
                .documentType(DocumentType.builder().title("MonitoringFeature").type("MonitoringFeature").build())
                .documentType(DocumentType.builder().title("ObservableProperty").type("ObservableProperty").build())
                .documentType(DocumentType.builder().title("ObservationPlaceholder").type("ObservationPlaceholder").build())
                .documentType(DocumentType.builder().title("OperatingProperty").type("OperatingProperty").build())
                .documentType(DocumentType.builder().title("OperatingRange").type("OperatingRange").build())
                .documentType(DocumentType.builder().title("Person").type("Person").build())
                .documentType(DocumentType.builder().title("SampleFeature").type("SampleFeature").build())
                .documentType(DocumentType.builder().title("Sensor").type("Sensor").build())
                .documentType(DocumentType.builder().title("SensorType").type("SensorType").build())
                .documentType(DocumentType.builder().title("SingleSystemDeployment").type("SingleSystemDeployment").build())
                .documentType(DocumentType.builder().title("Stimulus").type("Stimulus").build())
                .documentType(DocumentType.builder().title("SystemCapability").type("SystemCapability").build())
                .documentType(DocumentType.builder().title("SystemProperty").type("SystemProperty").build())
                .documentType(DocumentType.builder().title("TemporalProcedure").type("TemporalProcedure").build())
                .documentType(DocumentType.builder().title("VerticalMonitoringFeature").type("VerticalMonitoringFeature").build())

                .fileUpload(false)
                .build()
        );
    }
}
