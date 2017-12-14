package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;
import org.apache.jena.ext.com.google.common.collect.Lists;
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

    public String getPlanningDocs() {
        if (null != planningDocsOther) return planningDocsOther;
        return planningDocs;
    }

    private Funded nercFunded;
    private Funded publicFunded;

    private List<DatasetOffered> datasetsOffered = Lists.newArrayList();
    private DatasetOffered datasetOffered;

    private boolean hasRelatedDatasets;
    private List<String> relatedDatasets = Lists.newArrayList();

    public List<String> getRelatedDatasets () {
        relatedDatasets.removeIf(dataset -> dataset == null);
        return relatedDatasets;
    }

    private String scienceDomain;
    private String scienceDomainOther;

    public String getScienceDomain() {
        if (null != scienceDomainOther) return scienceDomainOther;
        return scienceDomain;
    }

    private boolean uniqueDeposit;
    private boolean modelOutput;
    private boolean publishedPaper;
    private boolean reusable;

    @Data
    public static class DatasetOffered {
        private String name;
        private String format;
        private String size;
        private String description;
        public int getDescriptionSize() {
            return description.split("\r\n|\r|\n").length;
        }
    }

    public enum Funded {
        no, yes, partly
    }
}



