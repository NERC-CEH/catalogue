package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.http.MediaType;

import static org.springframework.http.MediaType.parseMediaType;

public class CatalogueMediaTypes {
    public static final String BIBTEX_SHORT = "bib";
    public static final String BIBTEX_VALUE = "application/x-bibtex";
    public static final MediaType BIBTEX = parseMediaType(BIBTEX_VALUE);

    public static final String CEH_MODEL_SHORT = "ceh-model";
    public static final String CEH_MODEL_JSON_VALUE = "application/vnd.ceh.model+json";
    public static final MediaType CEH_MODEL_JSON = parseMediaType(CEH_MODEL_JSON_VALUE);

    public static final String CEH_MODEL_APPLICATION_SHORT = "ceh-model-application";
    public static final String CEH_MODEL_APPLICATION_JSON_VALUE = "application/vnd.ceh.model.application+json";
    public static final MediaType CEH_MODEL_APPLICATION_JSON = parseMediaType(CEH_MODEL_APPLICATION_JSON_VALUE);

    public static final String CSV_SHORT = "csv";
    public static final String TEXT_CSV_VALUE = "text/csv";
    public static final MediaType TEXT_CSV = parseMediaType(TEXT_CSV_VALUE);

    public static final String DATACITE_SHORT = "datacite";
    public static final String DATACITE_XML_VALUE = "application/x-datacite+xml";
    public static final MediaType DATACITE_XML = parseMediaType(DATACITE_XML_VALUE);

    public static final String DATA_TYPE_JSON_VALUE = "application/vnd.data-type+json";
    public static final String DATA_TYPE_SHORT = "data-type";
    public static final MediaType DATA_TYPE_JSON = parseMediaType(DATA_TYPE_JSON_VALUE);

    public static final String EF_INSPIRE_XML_SHORT = "efinspire";
    public static final String EF_INSPIRE_XML_VALUE = "application/vnd.ukeof.inspire+xml";
    public static final MediaType EF_INSPIRE_XML = parseMediaType(EF_INSPIRE_XML_VALUE);

    public static final String ELTER_SHORT = "elter";
    public static final String ELTER_JSON_VALUE = "application/vnd.elter+json";
    public static final MediaType ELTER_JSON = parseMediaType(ELTER_JSON_VALUE);

    public static final String LINKED_ELTER_SHORT = "linked-elter";
    public static final String LINKED_ELTER_JSON_VALUE = "application/vnd.linked-elter+json";
    public static final MediaType LINKED_ELTER_JSON = MediaType.parseMediaType(LINKED_ELTER_JSON_VALUE);

    public static final String NERC_MODEL = "nerc-model";
    public static final String NERC_MODEL_JSON_VALUE = "application/vnd.nerc-model+json";
    public static final MediaType NERC_MODEL_JSON = parseMediaType(NERC_MODEL_JSON_VALUE);

    public static final String NERC_MODEL_USE = "nerc-model-use";
    public static final String NERC_MODEL_USE_JSON_VALUE = "application/vnd.nerc-model-use+json";
    public static final MediaType NERC_MODEL_USE_JSON = parseMediaType(NERC_MODEL_USE_JSON_VALUE);

    public static final String ERAMMP_DATACUBE_SHORT = "erammp-datacube";
    public static final String ERAMMP_DATACUBE_JSON_VALUE = "application/vnd.erammp-datacube+json";
    public static final MediaType ERAMMP_DATACUBE_JSON = parseMediaType(ERAMMP_DATACUBE_JSON_VALUE);

    public static final String ERAMMP_MODEL_SHORT = "erammp-model";
    public static final String ERAMMP_MODEL_JSON_VALUE = "application/vnd.erammp-model+json";
    public static final MediaType ERAMMP_MODEL_JSON = MediaType.parseMediaType(ERAMMP_MODEL_JSON_VALUE);

    public static final String GEMINI_JSON_SHORT = "gemini-json";
    public static final String GEMINI_JSON_VALUE = "application/gemini+json";
    public static final MediaType GEMINI_JSON = parseMediaType(GEMINI_JSON_VALUE);

    public static final String GEMINI_XML_SHORT = "gemini";
    public static final String GEMINI_XML_VALUE = "application/x-gemini+xml";
    public static final MediaType GEMINI_XML = parseMediaType(GEMINI_XML_VALUE);

    public static final String LINKED_SHORT = "link";
    public static final String LINKED_JSON_VALUE = "application/link+json";
    public static final MediaType LINKED_JSON = parseMediaType(LINKED_JSON_VALUE);

    public static final String MAPSERVER_GML_VALUE = "application/vnd.ogc.gml";
    public static final MediaType MAPSERVER_GML = parseMediaType(MAPSERVER_GML_VALUE);

    public static final String MODEL_SHORT = "model";
    public static final String MODEL_JSON_VALUE = "application/model+json";
    public static final MediaType MODEL_JSON = parseMediaType(MODEL_JSON_VALUE);

    public static final String OSDP_AGENT_SHORT = "osdp-agent";
    public static final String OSDP_AGENT_JSON_VALUE = "application/vnd.osdp.agent+json";
    public static final MediaType OSDP_AGENT_JSON = parseMediaType(OSDP_AGENT_JSON_VALUE);

    public static final String OSDP_DATASET_SHORT = "osdp-dataset";
    public static final String OSDP_DATASET_JSON_VALUE = "application/vnd.osdp.dataset+json";
    public static final MediaType OSDP_DATASET_JSON = parseMediaType(OSDP_DATASET_JSON_VALUE);

    public static final String OSDP_MODEL_SHORT = "osdp-model";
    public static final String OSDP_MODEL_JSON_VALUE = "application/vnd.osdp.model+json";
    public static final MediaType OSDP_MODEL_JSON = parseMediaType(OSDP_MODEL_JSON_VALUE);

    public static final String OSDP_MONITORING_ACTIVITY_SHORT = "osdp-monitoring-activity";
    public static final String OSDP_MONITORING_ACTIVITY_JSON_VALUE = "application/vnd.osdp.monitoring-activity+json";
    public static final MediaType OSDP_MONITORING_ACTIVITY_JSON = parseMediaType(OSDP_MONITORING_ACTIVITY_JSON_VALUE);

    public static final String OSDP_MONITORING_FACILITY_SHORT = "osdp-monitoring-facility";
    public static final String OSDP_MONITORING_FACILITY_JSON_VALUE = "application/vnd.osdp.monitoring-facility+json";
    public static final MediaType OSDP_MONITORING_FACILITY_JSON = parseMediaType(OSDP_MONITORING_FACILITY_JSON_VALUE);

    public static final String OSDP_MONITORING_PROGRAMME_SHORT = "osdp-monitoring-programme";
    public static final String OSDP_MONITORING_PROGRAMME_JSON_VALUE = "application/vnd.osdp.monitoring-programme+json";
    public static final MediaType OSDP_MONITORING_PROGRAMME_JSON = parseMediaType(OSDP_MONITORING_PROGRAMME_JSON_VALUE);

    public static final String OSDP_PUBLICATION_SHORT = "osdp-publication";
    public static final String OSDP_PUBLICATION_JSON_VALUE = "application/vnd.osdp.publication+json";
    public static final MediaType OSDP_PUBLICATION_JSON = parseMediaType(OSDP_PUBLICATION_JSON_VALUE);

    public static final String OSDP_SAMPLE_SHORT = "osdp-sample";
    public static final String OSDP_SAMPLE_JSON_VALUE = "application/vnd.osdp.sample+json";
    public static final MediaType OSDP_SAMPLE_JSON = parseMediaType(OSDP_SAMPLE_JSON_VALUE);

    public static final String RDF_SCHEMAORG_SHORT = "schema.org";
    public static final String RDF_SCHEMAORG_VALUE = "application/vnd.schemaorg.ld+json";
    public static final MediaType RDF_SCHEMAORG_JSON = parseMediaType(RDF_SCHEMAORG_VALUE);

    public static final String RDF_TTL_SHORT = "ttl";
    public static final String RDF_TTL_VALUE = "text/turtle";
    public static final MediaType RDF_TTL = parseMediaType(RDF_TTL_VALUE);

    public static final String RESEARCH_INFO_SYSTEMS_SHORT  = "ris";
    public static final String RESEARCH_INFO_SYSTEMS_VALUE  = "application/x-research-info-systems";
    public static final MediaType RESEARCH_INFO_SYSTEMS = parseMediaType(RESEARCH_INFO_SYSTEMS_VALUE);

    public static final String SAMPLE_ARCHIVE_SHORT = "sample-archive";
    public static final String SAMPLE_ARCHIVE_JSON_VALUE = "application/vnd.sample-archive+json";
    public static final MediaType SAMPLE_ARCHIVE_JSON = parseMediaType(SAMPLE_ARCHIVE_JSON_VALUE);

    public static final String UKEOF_XML_SHORT = "ukeof";
    public static final String UKEOF_XML_VALUE = "application/ukeof+xml";
    public static final MediaType UKEOF_XML = parseMediaType(UKEOF_XML_VALUE);

    public static final String UPLOAD_DOCUMENT_SHORT = "upload";
    public static final String UPLOAD_DOCUMENT_JSON_VALUE = "application/vnd.upload-document+json";
    public static final MediaType UPLOAD_DOCUMENT_JSON = MediaType.parseMediaType(UPLOAD_DOCUMENT_JSON_VALUE);

    public static final String UKEMS_DOCUMENT_SHORT = "ukems-document";
    public static final String UKEMS_DOCUMENT_JSON_VALUE = "application/vnd.ukems-document+json";
    public static final MediaType UKEMS_DOCUMENT_JSON = MediaType.parseMediaType(UKEMS_DOCUMENT_JSON_VALUE);
}
