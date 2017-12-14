package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;

import lombok.Data;

@Data
public class DepositRequest {
    private String datasetTitle;
    private String depositorName;
    private String depositorEmail;
    private String depositorOtherContact;
    private String projectName;
    private String planningDocs;
    private String planningDocsOther;
    private Foolean nercFunded;
    private Foolean publicFunded;
    private List<DatasetOffered> datasetsOffered;
    private boolean hasRelatedDatasets;
    private List<String> relatedDatasets;
    private String scienceDomain;
    private String scienceDomainOther;
    private boolean uniqueDeposit;
    private boolean modelOutput;
    private boolean publishedPaper;
    private boolean reusable;

    @Data
    private class DatasetOffered {
        private String name;
        private String format;
        private String size;
        private String description;
    }

    private enum Foolean {
        no, yes, partly
    }
}



