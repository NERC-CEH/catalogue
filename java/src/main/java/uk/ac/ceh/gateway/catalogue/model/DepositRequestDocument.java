package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.springframework.util.StringUtils;

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

    private String getJiraRow(String name, String value) {
        String row = String.format("|| %s | %s |\\n", name, value);
        row = StringUtils.replace(row, "\r", "\\r");
        row = StringUtils.replace(row, "\n", "\\n");
        return row;
    }

    public String getJiraDescription() {
        return getJiraRow("Depositor Name", depositorName) +
            getJiraRow("Depositor Email", depositorEmail) +
            getJiraRow("Depositor Other Contact", depositorOtherContact) +
            getJiraRow("Project Name", projectName) +
            getJiraRow("Planning Docs", getPlanningDocs()) +
            getJiraRow("NERC Funded", nercFunded.toString()) +
            getJiraRow("Public Funded", publicFunded.toString()) +
            getJiraRow("Datasets Offered", getDatasetsOffered().stream().map(x -> x.toString()).collect(Collectors.joining("\n---\n"))) +
            getJiraRow("Related Datasets", getRelatedDatasets().stream().map(x -> x.toString()).collect(Collectors.joining("\n"))) +
            getJiraRow("Science Domain", getScienceDomain()) +
            getJiraRow("Unique Depsoit", uniqueDeposit ? "yes" : "no") +
            getJiraRow("Model Ouput", modelOutput ? "yes" : "no") +
            getJiraRow("Published Paper", publishedPaper ? "yes" : "no") +
            getJiraRow("Reusable", reusable ? "yes" : "no")
        ;
    }

    public enum Funded {
        no, yes, partly
    }
}



