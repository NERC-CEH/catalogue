package uk.ac.ceh.gateway.catalogue.services;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.MediaType;

import org.apache.jena.ext.com.google.common.collect.Lists;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import lombok.AllArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.model.JiraIssue;
import uk.ac.ceh.gateway.catalogue.model.JiraIssueCreate;
import uk.ac.ceh.gateway.catalogue.model.JiraSearchResults;

@Slf4j
@AllArgsConstructor
public class JiraService {
    private final WebResource resource;

    public static class IssueBuilder  {
        private final String project;
        private final String issueTypeName;
        private final String summary;
        private String description;
        private final Map<String, String> fields;
        private final List<String> components;
        private final List<String> labels;

        public IssueBuilder(String project, String issueTypeName, String summary) {
            this.project = project;
            this.issueTypeName = issueTypeName;
            this.summary = summary;
            fields = new HashMap<>();
            components = Lists.newArrayList();
            labels = Lists.newArrayList();
        }

        public IssueBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public IssueBuilder withField(String name, String value) {
            fields.put(name, value);
            return this;
        }

        public IssueBuilder withCompoent(String component) {
            components.add(component);
            return this;
        }

        public IssueBuilder withLabel(String label) {
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

    public JiraIssueCreate create (IssueBuilder builder) {
        String path = "issue";
        val input = builder.build();
        log.error("input {}", input);
        return resource
            .path(path)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .post(JiraIssueCreate.class, input);
    }

    public void comment (String key, String comment) {
        String path = String.format("issue/%s", key);
        String input = String.format("{\"update\":{\"comment\":[{\"add\":{\"body\":\"%s\"}}]}}", comment);
        resource
            .path(path)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .put(input);
    }

    public void transition (String key, String id) {        
        String path = String.format("issue/%s/transitions", key);
        String input = String.format("{\"transition\":{\"id\":\"%s\"}}", id);
        resource
            .path(path)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .post(input);
    }

    public List<JiraIssue> search (String jql) {
        return resource
            .path("search")
            .queryParam("jql", jql)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .get(ClientResponse.class)
            .getEntity(JiraSearchResults.class)
            .getIssues();
    }
}