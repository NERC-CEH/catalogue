package uk.ac.ceh.gateway.catalogue.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import lombok.val;

public class JiraIssueBuilder  {
    private final String project;
    private final String issueTypeName;
    private final String summary;
    private String description;
    private final Map<String, String> fields;
    private final List<String> components;
    private final List<String> labels;

    public JiraIssueBuilder(String project, String issueTypeName, String summary) {
        this.project = project;
        this.issueTypeName = issueTypeName;
        this.summary = summary;
        fields = new HashMap<>();
        components = Lists.newArrayList();
        labels = Lists.newArrayList();
    }

    public JiraIssueBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public JiraIssueBuilder withField(String name, String value) {
        fields.put(name, value);
        return this;
    }

    public JiraIssueBuilder withCompoent(String component) {
        components.add(component);
        return this;
    }

    public JiraIssueBuilder withLabel(String label) {
        labels.add(label);
        return this;
    }

    public String build () {
        String inputFields = "";
        if (fields.size() > 0) {
            for (val field : fields.entrySet()) {
                val key = field.getKey();
                val value = field.getValue();
                inputFields += String.format(", \"%s\":\"%s\"", key, value);
            }
        }

        String inputComponents = "";
        if (components.size() > 0) {
            for (val component : components)
                inputComponents += String.format("{\"name\": \"%s\"}", component);
            inputComponents = String.format(", \"components\": [%s]", inputComponents);
        }

        String inputLabels = "";
        if (labels.size() > 0) {
            for (val label : labels)
                inputLabels += String.format("\"%s\",", label);
            inputLabels = inputLabels.substring(0, inputLabels.length() - 1);
            inputLabels = String.format(", \"labels\": [%s]", inputLabels);
        }

        if (description != null) {
            description = String.format(", \"description\": \"%s\"", description);
        }

        return String.format(
            "{\"fields\": {\"project\": {\"key\": \"%s\"},\"issuetype\": {\"name\": \"%s\"},\"summary\": \"%s\" %s %s %s %s}}",
            project, issueTypeName, summary,
            inputFields, inputComponents, inputLabels, description
        );
    }
}