package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;
import org.apache.jena.ext.com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DepositRequestDocument extends AbstractMetadataDocument {
    private String depositorName;
    private String depositorEmail;
    private String depositorOtherContact = "";
    private String projectName = "";

    private String planningDocs;
    private String planningDocsOther;

    public String getPlanningDocs() {
        if (null != planningDocsOther) return planningDocsOther;
        return planningDocs;
    }

    private Funded nercFunded;
    private Funded publicFunded;

    private List<DatasetOffered> datasetsOffered = Lists.newArrayList();
    public List<DatasetOffered> getDatasetsOffered () {
        datasetsOffered.removeIf(dataset -> dataset.getName() == null);
        return datasetsOffered;
    }

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
        private String document;
        public int getDescriptionSize() {
            return description == null ? 1 : description.split("\r\n|\r|\n").length;
        }
    }

    public enum Funded {
        no, yes, partly
    }
}



