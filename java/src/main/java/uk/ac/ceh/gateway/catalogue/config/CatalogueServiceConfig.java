package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.catalogue.InMemoryCatalogueService;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.Catalogue.DocumentType;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabulary;

import java.util.List;
import java.util.stream.Collectors;

import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.*;

@Configuration
public class CatalogueServiceConfig {
    public static final String GEMINI_DOCUMENT = "GEMINI_DOCUMENT";
    public static final String EF_DOCUMENT = "EF_DOCUMENT";
    public static final String ELTER_DOCUMENT = "ELTER_DOCUMENT";
    public static final String LINKED_ELTER_DOCUMENT = "LINKED_ELTER_DOCUMENT";
    public static final String IMP_DOCUMENT = "IMP_DOCUMENT";
    public static final String CEH_MODEL = "CEH_MODEL";
    public static final String CEH_MODEL_APPLICATION = "CEH_MODEL_APPLICATION";
    public static final String LINK_DOCUMENT = "LINK_DOCUMENT";

    @Bean
    public CatalogueService catalogueService(List<KeywordVocabulary> vocabularies) {
        String defaultCatalogueKey = "eidc";

        DocumentType agent = DocumentType.builder()
                .title("Agent")
                .type(OSDP_AGENT_SHORT)
                .build();

        DocumentType dataset = DocumentType.builder()
                .title("Dataset")
                .type(OSDP_DATASET_SHORT)
                .build();

        DocumentType dataType = DocumentType.builder()
                .title("Data type")
                .type(DATA_TYPE_SHORT)
                .build();

        DocumentType erammpDatacube = DocumentType.builder()
                .title("ERAMMP data cube")
                .type(ERAMMP_DATACUBE_SHORT)
                .build();

        DocumentType erammpModel = DocumentType.builder()
                .title("ERAMMP model")
                .type(ERAMMP_MODEL_SHORT)
                .build();

        DocumentType cehModel = DocumentType.builder()
                .title("Model")
                .type(CEH_MODEL)
                .build();

        DocumentType cehModelApplication = DocumentType.builder()
                .title("Model use")
                .type(CEH_MODEL_APPLICATION)
                .build();

        DocumentType gemini = DocumentType.builder()
            .title("Data resource")
            .type(GEMINI_DOCUMENT)
            .build();

        DocumentType elter = DocumentType.builder()
            .title("eLTER dataset")
            .type(ELTER_DOCUMENT)
            .build();

        DocumentType linkedElter = DocumentType.builder()
            .title("Linked document")
            .type(LINKED_ELTER_DOCUMENT)
            .build();

        DocumentType imp = DocumentType.builder()
            .title("Model")
            .type(IMP_DOCUMENT)
            .build();

        DocumentType link = DocumentType.builder()
            .title("Link")
            .type(LINK_DOCUMENT)
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

        DocumentType ukems = DocumentType.builder()
            .title("UK-EMS Document")
            .type(UKEMS_DOCUMENT_SHORT)
            .build();

        return new InMemoryCatalogueService(
            defaultCatalogueKey,

            Catalogue.builder()
                .id("assist")
                .title("About ASSIST")
                .url("https://assist.ceh.ac.uk/content/about-assist")
                .facetKey("assistResearchThemes")
                .facetKey("assistTopics")
                .documentType(gemini)
                .documentType(cehModel)
                .documentType(cehModelApplication)
                .documentType(link)
                .vocabularies(getCatalogueVocabularies(vocabularies, "assist"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("cmp")
                .title("Catchment Management Modelling Platform")
                .url("https://www.cammp.org.uk")
                .facetKey("resourceType")
                .facetKey("impCaMMPIssues")
                .facetKey("impDataType")
                .facetKey("impScale")
                .facetKey("impTopic")
                .facetKey("impWaterPollutant")
                .documentType(gemini)
                .documentType(imp)
                .documentType(link)
                .vocabularies(getCatalogueVocabularies(vocabularies, "cmp"))
                .fileUpload(true)
                .build(),

            Catalogue.builder()
                .id("ceh")
                .title("UK Centre for Ecology & Hydrology")
                .url("https://www.ceh.ac.uk")
                .facetKey("topic")
                .facetKey("resourceType")
                .facetKey("licence")
                .vocabularies(getCatalogueVocabularies(vocabularies, "ceh"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("datalabs")
                .title("Datalabs")
                .url("https://datalab.datalabs.ceh.ac.uk/")
                .facetKey("resourceType")
                .documentType(gemini)
                .documentType(cehModel)
                .documentType(cehModelApplication)
                .documentType(ukems)
                .vocabularies(getCatalogueVocabularies(vocabularies, "datalabs"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("edge")
                .title("EDgE")
                .url("https://edge.climate.copernicus.eu")
                .facetKey("recordType")
                .documentType(gemini)
                .documentType(link)
                .vocabularies(getCatalogueVocabularies(vocabularies, "edge"))
                .fileUpload(true)
                .build(),

            Catalogue.builder()
                .id(defaultCatalogueKey)
                .title("EIDC")
                .url("https://www.eidc.ac.uk")
                .facetKey("topic")
                .facetKey("recordType")
                .facetKey("status")
                .facetKey("rightsHolder")
                .facetKey("funder")
                .documentType(gemini)
                .documentType(dataType)
                .vocabularies(getCatalogueVocabularies(vocabularies, defaultCatalogueKey))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("elter")
                .title("eLTER")
                .url("")
                .facetKey("elterDeimsSite")
                .documentType(elter)
                .documentType(linkedElter)
                .vocabularies(getCatalogueVocabularies(vocabularies, "elter"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("erammp")
                .title("ERAMMP")
                .url("")
                .facetKey("recordType")
                .facetKey("condition")
                .documentType(erammpModel)
                .documentType(erammpDatacube)
                .documentType(link)
                .vocabularies(getCatalogueVocabularies(vocabularies, "erammp"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("inlicensed")
                .title("UKCEH In-licensed Datasets")
                .url("http://intranet.ceh.ac.uk/procedures/commercialisation/data-licensing-ipr/in-licensed-data-list")
                .facetKey("resourceType")
                .documentType(gemini)
                .vocabularies(getCatalogueVocabularies(vocabularies, "inlicensed"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("inms")
                .title("International Nitrogen Management System")
                .url("https://data.inms.international/")
                .facetKey("recordType")
                .facetKey("impScale")
                .facetKey("impTopic")
                .facetKey("inmsPollutant")
                .facetKey("modelType")
                .facetKey("inmsDemonstrationRegion")
                .facetKey("inmsProject")
                .documentType(gemini)
                .documentType(cehModel)
                .documentType(cehModelApplication)
                .vocabularies(getCatalogueVocabularies(vocabularies, "inms"))
                .documentType(link)
                .fileUpload(true)
                .build(),

            Catalogue.builder()
                .id("m")
                .title("Model Management")
                .url("http://intranet.ceh.ac.uk/procedures/science-information-management/science-information-management-full-procedures/model-management-procedures")
                .facetKey("resourceType")
                .documentType(cehModel)
                .documentType(cehModelApplication)
                .vocabularies(getCatalogueVocabularies(vocabularies, "m"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("nc")
                .title("Natural Capital")
                .url("https://www.ceh.ac.uk")
                .facetKey("ncAssets")
                .facetKey("ncCaseStudy")
                .facetKey("ncDrivers")
                .facetKey("ncEcosystemServices")
                .facetKey("ncGeographicalScale")
                .documentType(gemini)
                .documentType(cehModel)
                .documentType(cehModelApplication)
                .documentType(link)
                .vocabularies(getCatalogueVocabularies(vocabularies, "nc"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("nm")
                .title("NERC EDS model catalogue")
                .url("")
                .facetKey("topic")
                .facetKey("resourceType")
                .facetKey("licence")
                .documentType(cehModel)
                .documentType(cehModelApplication)
                .vocabularies(getCatalogueVocabularies(vocabularies, "nm"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("osdp")
                .title("Open Soils Data Platform")
                .url("https://www.ceh.ac.uk")
                .documentType(agent)
                .documentType(dataset)
                .documentType(model)
                .documentType(monitoringActivity)
                .documentType(monitoringFacility)
                .documentType(monitoringProgramme)
                .documentType(publication)
                .documentType(sample)
                .facetKey("resourceType")
                .vocabularies(getCatalogueVocabularies(vocabularies, "osdp"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("sa")
                .title("Sample Archive")
                .url("")
                .facetKey("saTaxon")
                .documentType(sampleArchive)
                .fileUpload(false)
                .vocabularies(getCatalogueVocabularies(vocabularies, "sa"))
                .build(),

            Catalogue.builder()
                .id("ukscape")
                .title("UK-SCAPE")
                .url("https://www.ceh.ac.uk/ukscape")
                .facetKey("resourceType")
                .facetKey("licence")
                .documentType(gemini)
                .documentType(cehModel)
                .documentType(cehModelApplication)
                .documentType(link)
                .fileUpload(false)
                .vocabularies(getCatalogueVocabularies(vocabularies, "ukscape"))
                .build()
        );
    }

    private List<KeywordVocabulary> getCatalogueVocabularies(List<KeywordVocabulary> vocabularies, String catalogueId) {
        return vocabularies
            .stream()
            .filter(vocabulary -> vocabulary.usedInCatalogue(catalogueId))
            .collect(Collectors.toList());
    }
}
