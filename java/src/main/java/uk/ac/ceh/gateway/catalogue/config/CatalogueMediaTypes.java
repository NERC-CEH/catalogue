package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.http.MediaType;

public class CatalogueMediaTypes {
    public static final String BIBTEX_SHORT = "bib";
    public static final String BIBTEX_VALUE = "application/x-bibtex";
    public static final MediaType BIBTEX = MediaType.parseMediaType(BIBTEX_VALUE);

    public static final String CEH_MODEL_SHORT = "ceh-model";
    public static final String CEH_MODEL_JSON_VALUE = "application/vnd.ceh.model+json";
    public static final MediaType CEH_MODEL_JSON = MediaType.parseMediaType(CEH_MODEL_JSON_VALUE);

    public static final String CEH_MODEL_APPLICATION_SHORT = "ceh-model-application";
    public static final String CEH_MODEL_APPLICATION_JSON_VALUE = "application/vnd.ceh.model.application+json";
    public static final MediaType CEH_MODEL_APPLICATION_JSON = MediaType.parseMediaType(CEH_MODEL_APPLICATION_JSON_VALUE);

    public static final String DATACITE_SHORT = "datacite";
    public static final String DATACITE_XML_VALUE = "application/x-datacite+xml";
    public static final MediaType DATACITE_XML = MediaType.parseMediaType(DATACITE_XML_VALUE);

    public static final String DATA_TYPE_JSON_VALUE = "application/vnd.data-type+json";
    public static final String DATA_TYPE_SHORT = "data-type";
    public static final MediaType DATA_TYPE_JSON = MediaType.parseMediaType(DATA_TYPE_JSON_VALUE);

    public static final String EF_INSPIRE_XML_SHORT = "efinspire";
    public static final String EF_INSPIRE_XML_VALUE = "application/vnd.ukeof.inspire+xml";
    public static final MediaType EF_INSPIRE_XML = MediaType.parseMediaType(EF_INSPIRE_XML_VALUE);

    public static final String ELTER_JSON_SHORT = "elter-json";
    public static final String ELTER_JSON_VALUE = "application/vnd.elter+json";
    public static final MediaType ELTER_JSON = MediaType.parseMediaType(ELTER_JSON_VALUE);

    public static final String LINKED_ELTER_JSON_SHORT = "linked-elter-json";
    public static final String LINKED_ELTER_JSON_VALUE = "application/vnd.linked-elter+json";
    public static final MediaType LINKED_ELTER_JSON = MediaType.parseMediaType(LINKED_ELTER_JSON_VALUE);

    public static final String ERAMMP_DATACUBE_SHORT = "erammp-datacube";
    public static final String ERAMMP_DATACUBE_JSON_VALUE = "application/vnd.erammp-datacube+json";
    public static final MediaType ERAMMP_DATACUBE_JSON = MediaType.parseMediaType(ERAMMP_DATACUBE_JSON_VALUE);

    public static final String ERAMMP_MODEL_SHORT = "erammp-model";
    public static final String ERAMMP_MODEL_JSON_VALUE = "application/vnd.erammp-model+json";
    public static final MediaType ERAMMP_MODEL_JSON = MediaType.parseMediaType(ERAMMP_MODEL_JSON_VALUE);

    public static final String GEMINI_JSON_SHORT = "gemini-json";
    public static final String GEMINI_JSON_VALUE = "application/gemini+json";
    public static final MediaType GEMINI_JSON = MediaType.parseMediaType(GEMINI_JSON_VALUE);

    public static final String GEMINI_XML_SHORT = "gemini";
    public static final String GEMINI_XML_VALUE = "application/x-gemini+xml";
    public static final MediaType GEMINI_XML = MediaType.parseMediaType(GEMINI_XML_VALUE);

    public static final String LINKED_SHORT = "link";
    public static final String LINKED_JSON_VALUE = "application/link+json";
    public static final MediaType LINKED_JSON = MediaType.parseMediaType(LINKED_JSON_VALUE);

    public static final String MAPSERVER_GML_VALUE = "application/vnd.ogc.gml";
    public static final MediaType MAPSERVER_GML = MediaType.parseMediaType(MAPSERVER_GML_VALUE);

    public static final String MODEL_SHORT = "model";
    public static final String MODEL_JSON_VALUE = "application/model+json";
    public static final MediaType MODEL_JSON = MediaType.parseMediaType(MODEL_JSON_VALUE);

    public static final String OSDP_AGENT_SHORT = "osdp-agent";
    public static final String OSDP_AGENT_JSON_VALUE = "application/vnd.osdp.agent+json";
    public static final MediaType OSDP_AGENT_JSON = MediaType.parseMediaType(OSDP_AGENT_JSON_VALUE);

    public static final String OSDP_DATASET_SHORT = "osdp-dataset";
    public static final String OSDP_DATASET_JSON_VALUE = "application/vnd.osdp.dataset+json";
    public static final MediaType OSDP_DATASET_JSON = MediaType.parseMediaType(OSDP_DATASET_JSON_VALUE);

    public static final String OSDP_MODEL_SHORT = "osdp-model";
    public static final String OSDP_MODEL_JSON_VALUE = "application/vnd.osdp.model+json";
    public static final MediaType OSDP_MODEL_JSON = MediaType.parseMediaType(OSDP_MODEL_JSON_VALUE);

    public static final String OSDP_MONITORING_ACTIVITY_SHORT = "osdp-monitoring-activity";
    public static final String OSDP_MONITORING_ACTIVITY_JSON_VALUE = "application/vnd.osdp.monitoring-activity+json";
    public static final MediaType OSDP_MONITORING_ACTIVITY_JSON = MediaType.parseMediaType(OSDP_MONITORING_ACTIVITY_JSON_VALUE);

    public static final String OSDP_MONITORING_FACILITY_SHORT = "osdp-monitoring-facility";
    public static final String OSDP_MONITORING_FACILITY_JSON_VALUE = "application/vnd.osdp.monitoring-facility+json";
    public static final MediaType OSDP_MONITORING_FACILITY_JSON = MediaType.parseMediaType(OSDP_MONITORING_FACILITY_JSON_VALUE);

    public static final String OSDP_MONITORING_PROGRAMME_SHORT = "osdp-monitoring-programme";
    public static final String OSDP_MONITORING_PROGRAMME_JSON_VALUE = "application/vnd.osdp.monitoring-programme+json";
    public static final MediaType OSDP_MONITORING_PROGRAMME_JSON = MediaType.parseMediaType(OSDP_MONITORING_PROGRAMME_JSON_VALUE);

    public static final String OSDP_PUBLICATION_SHORT = "osdp-publication";
    public static final String OSDP_PUBLICATION_JSON_VALUE = "application/vnd.osdp.publication+json";
    public static final MediaType OSDP_PUBLICATION_JSON = MediaType.parseMediaType(OSDP_PUBLICATION_JSON_VALUE);

    public static final String OSDP_SAMPLE_SHORT = "osdp-sample";
    public static final String OSDP_SAMPLE_JSON_VALUE = "application/vnd.osdp.sample+json";
    public static final MediaType OSDP_SAMPLE_JSON = MediaType.parseMediaType(OSDP_SAMPLE_JSON_VALUE);

    public static final String RDF_SCHEMAORG_SHORT = "schema.org";
    public static final String RDF_SCHEMAORG_VALUE = "application/vnd.schemaorg.ld+json";
    public static final MediaType RDF_SCHEMAORG_JSON = MediaType.parseMediaType(RDF_SCHEMAORG_VALUE);

    public static final String RDF_TTL_SHORT = "ttl";
    public static final String RDF_TTL_VALUE = "text/turtle";
    public static final MediaType RDF_TTL = MediaType.parseMediaType(RDF_TTL_VALUE);

    public static final String RESEARCH_INFO_SYSTEMS_SHORT  = "ris";
    public static final String RESEARCH_INFO_SYSTEMS_VALUE  = "application/x-research-info-systems";
    public static final MediaType RESEARCH_INFO_SYSTEMS = MediaType.parseMediaType(RESEARCH_INFO_SYSTEMS_VALUE);

    public static final String SAMPLE_ARCHIVE_SHORT = "sample-archive";
    public static final String SAMPLE_ARCHIVE_JSON_VALUE = "application/vnd.sample-archive+json";
    public static final MediaType SAMPLE_ARCHIVE_JSON = MediaType.parseMediaType(SAMPLE_ARCHIVE_JSON_VALUE);

    public static final String UKEOF_XML_SHORT = "ukeof";
    public static final String UKEOF_XML_VALUE = "application/ukeof+xml";
    public static final MediaType UKEOF_XML = MediaType.parseMediaType(UKEOF_XML_VALUE);

    public static final String UPLOAD_DOCUMENT_SHORT = "upload";
    public static final String UPLOAD_DOCUMENT_JSON_VALUE = "application/vnd.upload-document+json";
    public static final MediaType UPLOAD_DOCUMENT_JSON = MediaType.parseMediaType(UPLOAD_DOCUMENT_JSON_VALUE);
}
