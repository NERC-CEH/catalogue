package uk.ac.ceh.gateway.catalogue;

import org.springframework.http.MediaType;

import static org.springframework.http.MediaType.parseMediaType;

public class CatalogueMediaTypes {
    public static final String BIBTEX_SHORT = "bib";
    public static final String BIBTEX_VALUE = "application/x-bibtex";
    public static final MediaType BIBTEX = parseMediaType(BIBTEX_VALUE);

    public static final String CEH_MODEL_JSON_VALUE = "application/vnd.ceh.model+json";
    public static final String CEH_MODEL_APPLICATION_JSON_VALUE = "application/vnd.ceh.model.application+json";

    public static final String CSV_SHORT = "csv";
    public static final String TEXT_CSV_VALUE = "text/csv";
    public static final MediaType TEXT_CSV = parseMediaType(TEXT_CSV_VALUE);

    public static final String DATACITE_SHORT = "datacite";
    public static final String DATACITE_XML_VALUE = "application/x-datacite+xml";
    public static final MediaType DATACITE_XML = parseMediaType(DATACITE_XML_VALUE);

    public static final String DATA_TYPE_JSON_VALUE = "application/vnd.data-type+json";
    public static final String CODE_JSON_VALUE = "application/vnd.code-document+json";

    public static final String EF_INSPIRE_XML_SHORT = "efinspire";
    public static final String EF_INSPIRE_XML_VALUE = "application/vnd.ukeof.inspire+xml";
    public static final MediaType EF_INSPIRE_XML = parseMediaType(EF_INSPIRE_XML_VALUE);

    public static final String ELTER_JSON_VALUE = "application/vnd.elter+json";
    public static final String ERAMMP_DATACUBE_JSON_VALUE = "application/vnd.erammp-datacube+json";
    public static final String ERAMMP_MODEL_JSON_VALUE = "application/vnd.erammp-model+json";
    public static final String GEMINI_JSON_VALUE = "application/gemini+json";

    public static final String INFRASTRUCTURERECORD_JSON_VALUE = "application/vnd.infrastructure+json";

    public static final String METHOD_JSON_VALUE = "application/vnd.method+json";

    public static final String GEMINI_XML_SHORT = "gemini";
    public static final String GEMINI_XML_VALUE = "application/x-gemini+xml";
    public static final MediaType GEMINI_XML = parseMediaType(GEMINI_XML_VALUE);

    public static final String LINKED_ELTER_JSON_VALUE = "application/vnd.linked-elter+json";
    public static final String LINKED_JSON_VALUE = "application/link+json";

    public static final String MAPSERVER_GML_VALUE = "application/vnd.ogc.gml";
    public static final MediaType MAPSERVER_GML = parseMediaType(MAPSERVER_GML_VALUE);

    public static final String MONITORING_ACTIVITY_JSON_VALUE = "application/vnd.monitoring-activity+json";
    public static final String MONITORING_FACILITY_JSON_VALUE = "application/vnd.monitoring-facility+json";
    public static final String MONITORING_NETWORK_JSON_VALUE = "application/vnd.monitoring-network+json";
    public static final String MONITORING_PROGRAMME_JSON_VALUE = "application/vnd.monitoring-programme+json";

    public static final String MODEL_JSON_VALUE = "application/model+json";
    public static final String NERC_MODEL_JSON_VALUE = "application/vnd.nerc-model+json";
    public static final String NERC_MODEL_USE_JSON_VALUE = "application/vnd.nerc-model-use+json";

    public static final String OSDP_AGENT_JSON_VALUE = "application/vnd.osdp.agent+json";
    public static final String OSDP_DATASET_JSON_VALUE = "application/vnd.osdp.dataset+json";
    public static final String OSDP_MODEL_JSON_VALUE = "application/vnd.osdp.model+json";
    public static final String OSDP_PUBLICATION_JSON_VALUE = "application/vnd.osdp.publication+json";
    public static final String OSDP_SAMPLE_JSON_VALUE = "application/vnd.osdp.sample+json";

    public static final String RDF_SCHEMAORG_SHORT = "schema.org";
    public static final String RDF_SCHEMAORG_VALUE = "application/vnd.schemaorg.ld+json";
    public static final MediaType RDF_SCHEMAORG_JSON = parseMediaType(RDF_SCHEMAORG_VALUE);

    public static final String ROCRATE_SHORT = "rocrate";
    public static final String ROCRATE_VALUE = "application/vnd.rocrate.ld+json";
    public static final MediaType ROCRATE_JSON = parseMediaType(ROCRATE_VALUE);

    public static final String ROCRATE_ATTACHED_SHORT = "rocrate-attached";
    public static final String ROCRATE_ATTACHED_VALUE = "application/vnd.rocrate-attached.ld+json";
    public static final MediaType ROCRATE_ATTACHED_JSON = parseMediaType(ROCRATE_ATTACHED_VALUE);

    public static final String CEDA_YAML_SHORT = "ceda";
    public static final String CEDA_YAML_VALUE = "application/ceda+json";
    public static final MediaType CEDA_YAML_JSON = parseMediaType(CEDA_YAML_VALUE);

    public static final String RDF_TTL_SHORT = "ttl";
    public static final String RDF_TTL_VALUE = "text/turtle";
    public static final MediaType RDF_TTL = parseMediaType(RDF_TTL_VALUE);

    public static final String RESEARCH_INFO_SYSTEMS_SHORT  = "ris";
    public static final String RESEARCH_INFO_SYSTEMS_VALUE  = "application/x-research-info-systems";
    public static final MediaType RESEARCH_INFO_SYSTEMS = parseMediaType(RESEARCH_INFO_SYSTEMS_VALUE);

    public static final String SAMPLE_ARCHIVE_JSON_VALUE = "application/vnd.sample-archive+json";

    public static final String UKEMS_DOCUMENT_JSON_VALUE = "application/vnd.ukems-document+json";
}
