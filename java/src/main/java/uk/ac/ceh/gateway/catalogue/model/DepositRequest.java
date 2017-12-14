package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;
import org.apache.jena.ext.com.google.common.collect.Lists;
import lombok.Data;
import lombok.Value;

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

    private Foolean nercFunded;
    private Foolean publicFunded;

    private List<String> datasetsOfferedName;
    private List<String> datasetsOfferedFormat;
    private List<String> datasetsOfferedSize;
    private List<String> datasetsOfferedDescription;

    public List<DatasetOffered> getDatasetsOffered() {
        List<DatasetOffered> datasetsOffered = Lists.newArrayList();
        for (int i = 0; i <  datasetsOfferedName.size(); i++)
            if (null != datasetsOfferedName.get(i))
                datasetsOffered.add(new DatasetOffered(datasetsOfferedName.get(i), datasetsOfferedFormat.get(i), datasetsOfferedSize.get(i), datasetsOfferedDescription.get(i)));
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

    @Value
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



