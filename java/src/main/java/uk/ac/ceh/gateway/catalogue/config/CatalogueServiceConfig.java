package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.catalogue.InMemoryCatalogueService;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabulary;

import java.util.List;
import java.util.stream.Collectors;

import static uk.ac.ceh.gateway.catalogue.DocumentTypes.*;

@SuppressWarnings("HttpUrlsUsage")
@Configuration
public class CatalogueServiceConfig {

    @Bean
    @Profile("server:datalabs")
    public CatalogueService datalabsCatalogue(List<KeywordVocabulary> vocabularies) {
        String defaultCatalogueKey = "datalabs";

        return new InMemoryCatalogueService(
            defaultCatalogueKey,

            Catalogue.builder()
                .id(defaultCatalogueKey)
                .title("Datalabs")
                .url("https://datalab.datalabs.ceh.ac.uk/")
                .contactUrl("https://nerc-datalabs.slack.com/")
                .facetKey("resourceType")
                .documentType(GEMINI_TYPE)
                .documentType(CEH_MODEL_TYPE)
                .documentType(CEH_MODEL_APPLICATION_TYPE)
                .documentType(DATALABS_TYPE)
                .documentType(UKEMS_TYPE)
                .vocabularies(getCatalogueVocabularies(vocabularies, defaultCatalogueKey))
                .fileUpload(false)
                .build()
        );
    }

    @Bean
    @Profile("server:eidc")
    public CatalogueService eidcCatalogue(List<KeywordVocabulary> vocabularies) {
        String defaultCatalogueKey = "eidc";

        return new InMemoryCatalogueService(
            defaultCatalogueKey,

            Catalogue.builder()
                .id("assist")
                .title("About ASSIST")
                .url("https://assist.ceh.ac.uk/content/about-assist")
                .contactUrl("https://assist.ceh.ac.uk/content/contact-assist")
                .facetKey("assistResearchThemes")
                .facetKey("assistTopics")
                .documentType(GEMINI_TYPE)
                .documentType(CEH_MODEL_TYPE)
                .documentType(CEH_MODEL_APPLICATION_TYPE)
                .documentType(LINK_TYPE)
                .vocabularies(getCatalogueVocabularies(vocabularies, "assist"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("cmp")
                .title("Catchment Management Modelling Platform")
                .url("https://www.cammp.org.uk")
                .contactUrl("http://www.cammp.org.uk/contact")
                .facetKey("resourceType")
                .facetKey("impCaMMPIssues")
                .facetKey("impDataType")
                .facetKey("impScale")
                .facetKey("impTopic")
                .facetKey("impWaterPollutant")
                .documentType(GEMINI_TYPE)
                .documentType(IMP_TYPE)
                .documentType(LINK_TYPE)
                .vocabularies(getCatalogueVocabularies(vocabularies, "cmp"))
                .fileUpload(true)
                .build(),

            Catalogue.builder()
                .id("ceh")
                .title("UK Centre for Ecology & Hydrology")
                .url("https://www.ceh.ac.uk")
                .contactUrl("https://www.ceh.ac.uk/contact-us")
                .facetKey("topic")
                .facetKey("resourceType")
                .facetKey("licence")
                .vocabularies(getCatalogueVocabularies(vocabularies, "ceh"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("edge")
                .title("EDgE")
                .url("https://edge.climate.copernicus.eu")
                .contactUrl("https://edge.climate.copernicus.eu/")
                .facetKey("recordType")
                .documentType(GEMINI_TYPE)
                .documentType(LINK_TYPE)
                .vocabularies(getCatalogueVocabularies(vocabularies, "edge"))
                .fileUpload(true)
                .build(),

            Catalogue.builder()
                .id(defaultCatalogueKey)
                .title("EIDC")
                .url("https://www.eidc.ac.uk")
                .contactUrl("https://www.eidc.ac.uk/contact")
                .facetKey("topic")
                .facetKey("recordType")
                .facetKey("status")
                .facetKey("rightsHolder")
                .facetKey("funder")
                .documentType(GEMINI_TYPE)
                .documentType(DATA_TYPE_TYPE)
                .vocabularies(getCatalogueVocabularies(vocabularies, defaultCatalogueKey))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("erammp")
                .title("ERAMMP")
                .url("")
                .contactUrl("")
                .facetKey("recordType")
                .facetKey("condition")
                .documentType(ERAMMP_MODEL_TYPE)
                .documentType(ERAMMP_DATACUBE_TYPE)
                .documentType(LINK_TYPE)
                .vocabularies(getCatalogueVocabularies(vocabularies, "erammp"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("inlicensed")
                .title("UKCEH In-licensed Datasets")
                .url("http://intranet.ceh.ac.uk/procedures/commercialisation/data-licensing-ipr/in-licensed-data-list")
                .contactUrl("https://www.ceh.ac.uk/contact-us")
                .facetKey("resourceType")
                .documentType(GEMINI_TYPE)
                .vocabularies(getCatalogueVocabularies(vocabularies, "inlicensed"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("m")
                .title("Model Management")
                .url("http://intranet.ceh.ac.uk/procedures/science-information-management/science-information-management-full-procedures/model-management-procedures")
                .contactUrl("https://www.ceh.ac.uk/contact-us")
                .facetKey("resourceType")
                .documentType(CEH_MODEL_TYPE)
                .documentType(CEH_MODEL_APPLICATION_TYPE)
                .vocabularies(getCatalogueVocabularies(vocabularies, "m"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("nc")
                .title("Natural Capital")
                .url("https://www.ceh.ac.uk")
                .contactUrl("https://www.ceh.ac.uk/contact-us")
                .facetKey("ncAssets")
                .facetKey("ncCaseStudy")
                .facetKey("ncDrivers")
                .facetKey("ncEcosystemServices")
                .facetKey("ncGeographicalScale")
                .documentType(GEMINI_TYPE)
                .documentType(CEH_MODEL_TYPE)
                .documentType(CEH_MODEL_APPLICATION_TYPE)
                .documentType(LINK_TYPE)
                .vocabularies(getCatalogueVocabularies(vocabularies, "nc"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("nm")
                .title("NERC EDS model catalogue")
                .url("https://nerc.ukri.org/research/sites/environmental-data-service-eds/")
                .contactUrl("https://nerc.ukri.org/research/sites/environmental-data-service-eds/")
                .facetKey("topic")
                .facetKey("resourceType")
                .facetKey("licence")
                .documentType(NERC_MODEL_TYPE)
                .documentType(NERC_MODEL_USE_TYPE)
                .vocabularies(getCatalogueVocabularies(vocabularies, "nm"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("osdp")
                .title("Open Soils Data Platform")
                .url("https://www.ceh.ac.uk")
                .contactUrl("https://www.ceh.ac.uk/contact-us")
                .documentType(OSDP_AGENT_TYPE)
                .documentType(OSDP_DATASET_TYPE)
                .documentType(OSDP_MODEL_TYPE)
                .documentType(OSDP_MONITORING_ACTIVITY_TYPE)
                .documentType(OSDP_MONITORING_FACILITY_TYPE)
                .documentType(OSDP_MONITORING_PROGRAMME_TYPE)
                .documentType(OSDP_PUBLICATION_TYPE)
                .documentType(OSDP_SAMPLE_TYPE)
                .facetKey("resourceType")
                .vocabularies(getCatalogueVocabularies(vocabularies, "osdp"))
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("sa")
                .title("UK Environmental Specimen Bank")
                .url("https://www.ceh.ac.uk/")
                .contactUrl("https://www.ceh.ac.uk/contact-us")
                .facetKey("saTaxon")
                .documentType(SAMPLE_ARCHIVE_TYPE)
                .fileUpload(false)
                .vocabularies(getCatalogueVocabularies(vocabularies, "sa"))
                .build(),

            Catalogue.builder()
                .id("ukscape")
                .title("UK-SCAPE")
                .url("https://uk-scape.ceh.ac.uk/resources/digital-assets-catalogue")
                .contactUrl("https://ukscape.ceh.ac.uk/about/contact-us")
                .facetKey("ukscapeResearchTheme")
                .facetKey("ukscapeScienceChallenge")
                .facetKey("ukscapeResearchProject")
                .facetKey("resourceType")
                .facetKey("ukscapeService")
                .facetKey("status")
                .documentType(GEMINI_TYPE)
                .documentType(CEH_MODEL_TYPE)
                .documentType(CEH_MODEL_APPLICATION_TYPE)
                .documentType(LINK_TYPE)
                .fileUpload(false)
                .vocabularies(getCatalogueVocabularies(vocabularies, "ukscape"))
                .build()
        );
    }

    @Bean
    @Profile("server:elter")
    public CatalogueService elterCatalogue(List<KeywordVocabulary> vocabularies) {
        String defaultCatalogueKey = "elter";

        return new InMemoryCatalogueService(
            defaultCatalogueKey,

            Catalogue.builder()
                .id(defaultCatalogueKey)
                .title("eLTER")
                .url("https://www.lter-europe.net/")
                .contactUrl("https://www.lter-europe.net/lter-europe/about/contacts")
                .facetKey("elterDeimsSite")
                .documentType(ELTER_TYPE)
                .documentType(LINKED_ELTER_TYPE)
                .vocabularies(getCatalogueVocabularies(vocabularies, defaultCatalogueKey))
                .fileUpload(false)
                .build()
        );
    }

    @Bean
    @Profile("server:inms")
    public CatalogueService inmsCatalogue(List<KeywordVocabulary> vocabularies) {
        String defaultCatalogueKey = "inms";

        return new InMemoryCatalogueService(
            defaultCatalogueKey,

            Catalogue.builder()
                .id(defaultCatalogueKey)
                .title("International Nitrogen Management System")
                .url("https://data.inms.international/")
                .contactUrl("https://www.inms.international/contact")
                .facetKey("recordType")
                .facetKey("impScale")
                .facetKey("impTopic")
                .facetKey("inmsPollutant")
                .facetKey("modelType")
                .facetKey("inmsDemonstrationRegion")
                .facetKey("inmsProject")
                .documentType(GEMINI_TYPE)
                .documentType(CEH_MODEL_TYPE)
                .documentType(CEH_MODEL_APPLICATION_TYPE)
                .documentType(LINK_TYPE)
                .vocabularies(getCatalogueVocabularies(vocabularies, defaultCatalogueKey))
                .fileUpload(true)
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
