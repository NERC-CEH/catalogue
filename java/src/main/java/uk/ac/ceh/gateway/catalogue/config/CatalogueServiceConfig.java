package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.catalogue.InMemoryCatalogueService;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;

import static uk.ac.ceh.gateway.catalogue.DocumentTypes.*;

@SuppressWarnings("HttpUrlsUsage")
@Configuration
public class CatalogueServiceConfig {

    @Bean
    @Profile("server:datalabs")
    public CatalogueService datalabsCatalogue() {
        String defaultCatalogueKey = "datalabs";

        return new InMemoryCatalogueService(
            defaultCatalogueKey,

            Catalogue.builder()
                .id(defaultCatalogueKey)
                .title("Datalabs")
                .url("https://datalab.datalabs.ceh.ac.uk/")
                .contactUrl("https://nerc-datalabs.slack.com/")
                .logo("datalabs.png")
                .facetKey("resourceType")
                .documentType(GEMINI_TYPE)
                .documentType(CEH_MODEL_TYPE)
                .documentType(CEH_MODEL_APPLICATION_TYPE)
                .documentType(CODE_TYPE)
                .documentType(UKEMS_TYPE)
                .fileUpload(false)
                .build()
        );
    }

    @Bean
    @Profile("server:eidc")
    public CatalogueService eidcCatalogue() {
        String defaultCatalogueKey = "eidc";

        return new InMemoryCatalogueService(
            defaultCatalogueKey,

            Catalogue.builder()
                .id("assist")
                .title("About ASSIST")
                .url("https://assist.ceh.ac.uk/content/about-assist")
                .contactUrl("https://assist.ceh.ac.uk/content/contact-assist")
                .logo("ukceh.png")
                .facetKey("assistResearchThemes")
                .facetKey("assistTopics")
                .documentType(GEMINI_TYPE)
                .documentType(CEH_MODEL_TYPE)
                .documentType(CEH_MODEL_APPLICATION_TYPE)
                .documentType(LINK_TYPE)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("cmp")
                .title("Catchment Management Modelling Platform")
                .url("https://www.cammp.org.uk")
                .contactUrl("http://www.cammp.org.uk/contact")
                .logo("ukceh.png")
                .facetKey("resourceType")
                .facetKey("impCaMMPIssues")
                .facetKey("impDataType")
                .facetKey("impScale")
                .facetKey("impTopic")
                .facetKey("impWaterPollutant")
                .documentType(GEMINI_TYPE)
                .documentType(IMP_TYPE)
                .documentType(LINK_TYPE)
                .fileUpload(true)
                .build(),

            Catalogue.builder()
                .id("ukceh")
                .title("UKCEH digital assets")
                .url("https://www.ceh.ac.uk")
                .contactUrl("https://www.ceh.ac.uk/contact-us")
                .logo("ukceh.png")
                .facetKey("ukcehResearchTheme")
                .facetKey("ukcehScienceChallenge")
                .facetKey("ukcehResearchProject")
                .facetKey("ukcehService")
                .facetKey("recordType")
                .documentType(GEMINI_TYPE)
                .documentType(METHODRECORD_TYPE)
                .documentType(MONITORING_FACILITY_TYPE)
                .documentType(MONITORING_NETWORK_TYPE)
                .documentType(MONITORING_PROGRAMME_TYPE)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id(defaultCatalogueKey)
                .title("EIDC")
                .url("https://www.eidc.ac.uk")
                .contactUrl("https://www.eidc.ac.uk/contact")
                .logo("eidc.png")
                .facetKey("topic")
                .facetKey("recordType")
                .facetKey("status")
                .facetKey("rightsHolder")
                .facetKey("funder")
                .documentType(GEMINI_TYPE)
                .documentType(DATA_TYPE_TYPE)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("infrastructure")
                .title("UKCEH Science Infrastructure Catalogue")
                .url("https://www.ceh.ac.uk/science-infrastructure/catalogue")
                .contactUrl("https://www.ceh.ac.uk/contact-us")
                .logo("ukceh.png")
                .facetKey("infrastructureClass")
                .facetKey("infrastructureCategory")
                .facetKey("infrastructureScale")
                .facetKey("infrastructureChallenge")
                .documentType(INFRASTRUCTURERECORD_TYPE)
                .documentType(LINK_TYPE)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("inlicensed")
                .title("UKCEH In-licensed Datasets")
                .url("http://intranet.ceh.ac.uk/procedures/commercialisation/data-licensing-ipr/in-licensed-data-list")
                .contactUrl("https://www.ceh.ac.uk/contact-us")
                .logo("ukceh.png")
                .facetKey("resourceType")
                .documentType(GEMINI_TYPE)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("m")
                .title("Model Management")
                .url("http://intranet.ceh.ac.uk/procedures/science-information-management/science-information-management-full-procedures/model-management-procedures")
                .contactUrl("https://www.ceh.ac.uk/contact-us")
                .logo("ukceh.png")
                .facetKey("resourceType")
                .documentType(CEH_MODEL_TYPE)
                .documentType(CEH_MODEL_APPLICATION_TYPE)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("nc")
                .title("Natural Capital")
                .url("https://www.ceh.ac.uk")
                .contactUrl("https://www.ceh.ac.uk/contact-us")
                .logo("ukceh.png")
                .facetKey("ncAssets")
                .facetKey("ncCaseStudy")
                .facetKey("ncDrivers")
                .facetKey("ncEcosystemServices")
                .facetKey("ncGeographicalScale")
                .documentType(GEMINI_TYPE)
                .documentType(CEH_MODEL_TYPE)
                .documentType(CEH_MODEL_APPLICATION_TYPE)
                .documentType(LINK_TYPE)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("nm")
                .title("NERC EDS model catalogue")
                .url("https://nerc.ukri.org/research/sites/environmental-data-service-eds/")
                .contactUrl("https://nerc.ukri.org/research/sites/environmental-data-service-eds/")
                .logo("ukceh.png")
                .facetKey("topic")
                .facetKey("resourceType")
                .facetKey("licence")
                .documentType(NERC_MODEL_TYPE)
                .documentType(NERC_MODEL_USE_TYPE)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("osdp")
                .title("Open Soils Data Platform")
                .url("https://www.ceh.ac.uk")
                .contactUrl("https://www.ceh.ac.uk/contact-us")
                .logo("ukceh.png")
                .documentType(OSDP_AGENT_TYPE)
                .documentType(OSDP_DATASET_TYPE)
                .documentType(OSDP_MODEL_TYPE)
                .documentType(MONITORING_ACTIVITY_TYPE)
                .documentType(MONITORING_FACILITY_TYPE)
                .documentType(MONITORING_PROGRAMME_TYPE)
                .documentType(OSDP_PUBLICATION_TYPE)
                .documentType(OSDP_SAMPLE_TYPE)
                .facetKey("resourceType")
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("sa")
                .title("UK Environmental Specimen Bank")
                .url("https://uk-scape.ceh.ac.uk/our-science/projects/UK-vESB")
                .contactUrl("https://www.ceh.ac.uk/contact-us")
                .logo("ukceh.png")
                .facetKey("saPhysicalState")
                .facetKey("saSpecimenType")
                .facetKey("saTaxon")
                .facetKey("saTissue")
                .documentType(SAMPLE_ARCHIVE_TYPE)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("ukscape")
                .title("UK-SCAPE")
                .url("https://uk-scape.ceh.ac.uk/resources/digital-assets-catalogue")
                .contactUrl("https://ukscape.ceh.ac.uk/about/contact-us")
                .logo("ukceh.png")
                .facetKey("ukcehResearchTheme")
                .facetKey("ukcehScienceChallenge")
                .facetKey("ukcehResearchProject")
                .facetKey("ukcehService")
                .facetKey("resourceType")
                .facetKey("status")
                .documentType(GEMINI_TYPE)
                .documentType(CEH_MODEL_TYPE)
                .documentType(CEH_MODEL_APPLICATION_TYPE)
                .documentType(LINK_TYPE)
                .fileUpload(false)
                .build(),

            Catalogue.builder()
                .id("ukeof")
                .title("UK Environmental Observation Framework")
                .url("https://www.ukeof.org.uk/")
                .contactUrl("https://www.ukeof.org.uk/contact")
                .logo("ukeof.png")
                .facetKey("resourceType")
                .facetKey("environmentalDomains")
                .facetKey("facilityType")
                .facetKey("pointOfContact")
                .facetKey("operationalStatus")
                .documentType(MONITORING_FACILITY_TYPE)
                .documentType(MONITORING_NETWORK_TYPE)
                .documentType(MONITORING_PROGRAMME_TYPE)
                .fileUpload(false)
                .build()
        );
    }

    @Bean
    @Profile("server:inms")
    public CatalogueService inmsCatalogue() {
        String defaultCatalogueKey = "inms";

        return new InMemoryCatalogueService(
            defaultCatalogueKey,

            Catalogue.builder()
                .id(defaultCatalogueKey)
                .title("International Nitrogen Management System")
                .url("https://www.inms.international/")
                .contactUrl("https://www.inms.international/contact")
                .logo("inms.png")
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
                .fileUpload(true)
                .build()
        );
    }

    @Bean
    @Profile("server:pimfe")
    public CatalogueService pimfeCatalogue() {
        String defaultCatalogueKey = "pimfe";

        return new InMemoryCatalogueService(
            defaultCatalogueKey,

            Catalogue.builder()
                .id(defaultCatalogueKey)
                .title("pIMFe Digital Asset Register")
                .url("")
                .contactUrl("")
                .logo("ukceh.png")
                .facetKey("resourceType")
                .documentType(GEMINI_TYPE)
                .documentType(CODE_TYPE)
                .documentType(NERC_MODEL_TYPE)
                .documentType(NERC_MODEL_USE_TYPE)
                .fileUpload(true)
                .build()
        );
    }
}
