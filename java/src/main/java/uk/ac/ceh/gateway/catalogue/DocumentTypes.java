package uk.ac.ceh.gateway.catalogue;

import static uk.ac.ceh.gateway.catalogue.catalogue.Catalogue.DocumentType;

public class DocumentTypes {

    public static final String CEH_MODEL = "CEH_MODEL";
    public static DocumentType CEH_MODEL_TYPE = DocumentType.builder()
        .title("Model")
        .type(CEH_MODEL)
        .build();

    public static final String CEH_MODEL_APPLICATION = "CEH_MODEL_APPLICATION";
    public static DocumentType CEH_MODEL_APPLICATION_TYPE = DocumentType.builder()
        .title("Model use")
        .type(CEH_MODEL_APPLICATION)
        .build();

    public static final String DATA_TYPE = "data-type";
    public static DocumentType DATA_TYPE_TYPE = DocumentType.builder()
        .title("Data type")
        .type(DATA_TYPE)
        .build();

    public static final String CODE = "code-document";
    public static DocumentType CODE_TYPE = DocumentType.builder()
        .title("Code/notebook")
        .type(CODE)
        .build();

    public static final String EF_DOCUMENT = "EF_DOCUMENT";

    public static final String INFRASTRUCTURERECORD = "infrastructurerecord";
        public static DocumentType INFRASTRUCTURERECORD_TYPE = DocumentType.builder()
            .title("Science infrastructure")
            .type(INFRASTRUCTURERECORD)
            .build();

    public static final String METHODRECORD = "methodrecord";
        public static DocumentType METHODRECORD_TYPE = DocumentType.builder()
            .title("Method")
            .type(METHODRECORD)
            .build();

    public static final String GEMINI = "GEMINI_DOCUMENT";
    public static DocumentType GEMINI_TYPE = DocumentType.builder()
        .title("Data resource")
        .type(GEMINI)
        .build();

    public static final String IMP = "IMP_DOCUMENT";
    public static DocumentType IMP_TYPE = DocumentType.builder()
        .title("Model")
        .type(IMP)
        .build();

    public static final String LINK = "LINK_DOCUMENT";
    public static DocumentType LINK_TYPE = DocumentType.builder()
        .title("Link")
        .type(LINK)
        .build();

    public static final String MONITORING_ACTIVITY = "monitoring-activity";
    public static DocumentType MONITORING_ACTIVITY_TYPE = DocumentType.builder()
        .title("Monitoring activity")
        .type(MONITORING_ACTIVITY)
        .build();

    public static final String MONITORING_FACILITY = "monitoring-facility";
    public static DocumentType MONITORING_FACILITY_TYPE = DocumentType.builder()
        .title("Monitoring facility")
        .type(MONITORING_FACILITY)
        .build();

    public static final String MONITORING_NETWORK = "monitoring-network";
    public static DocumentType MONITORING_NETWORK_TYPE = DocumentType.builder()
        .title("Monitoring network")
        .type(MONITORING_NETWORK)
        .build();

    public static final String MONITORING_PROGRAMME = "monitoring-programme";
    public static DocumentType MONITORING_PROGRAMME_TYPE = DocumentType.builder()
        .title("Monitoring programme")
        .type(MONITORING_PROGRAMME)
        .build();

    public static final String NERC_MODEL = "nerc-model";
    public static DocumentType NERC_MODEL_TYPE = DocumentType.builder()
        .title("Model code")
        .type(NERC_MODEL)
        .build();

    public static final String NERC_MODEL_USE = "nerc-model-use";
    public static DocumentType NERC_MODEL_USE_TYPE = DocumentType.builder()
        .title("Model implementation")
        .type(NERC_MODEL_USE)
        .build();

    public static final String OSDP_AGENT = "osdp-agent";
    public static DocumentType OSDP_AGENT_TYPE = DocumentType.builder()
        .title("Agent")
        .type(OSDP_AGENT)
        .build();

    public static final String OSDP_DATASET = "osdp-dataset";
    public static DocumentType OSDP_DATASET_TYPE = DocumentType.builder()
        .title("Dataset")
        .type(OSDP_DATASET)
        .build();

    public static final String OSDP_MODEL = "osdp-model";
    public static DocumentType OSDP_MODEL_TYPE = DocumentType.builder()
        .title("Model")
        .type(OSDP_MODEL)
        .build();

    public static final String OSDP_PUBLICATION = "osdp-publication";
    public static DocumentType OSDP_PUBLICATION_TYPE = DocumentType.builder()
        .title("Publication")
        .type(OSDP_PUBLICATION)
        .build();

    public static final String OSDP_SAMPLE = "osdp-sample";
    public static DocumentType OSDP_SAMPLE_TYPE = DocumentType.builder()
        .title("Sample")
        .type(OSDP_SAMPLE)
        .build();

    public static final String SAMPLE_ARCHIVE = "sample-archive";
    public static DocumentType SAMPLE_ARCHIVE_TYPE = DocumentType.builder()
        .title("Sample Archive")
        .type(SAMPLE_ARCHIVE)
        .build();

    public static final String SERVICE_AGREEMENT = "service-agreement";

    public static final String UKEMS_DOCUMENT = "ukems-document";
    public static DocumentType UKEMS_TYPE = DocumentType.builder()
        .title("UK-EMS Document")
        .type(UKEMS_DOCUMENT)
        .build();
}
