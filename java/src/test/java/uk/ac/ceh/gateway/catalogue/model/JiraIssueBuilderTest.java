package uk.ac.ceh.gateway.catalogue.model;

import org.junit.Test;
import lombok.val;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

public class JiraIssueBuilderTest {

    @Test
    public void basicBuild () {
        val built = new JiraIssueBuilder("project", "issueTypeName", "summary")
            .withCompoent("component")
            .withDescription("description")
            .withField("name0", "value0")
            .withField("name1", "value1")
            .withLabel("label0")
            .withLabel("label1")
            .build();

        assertThat(built, equalTo("{\"fields\": {\"project\": {\"key\": \"project\"},\"issuetype\": {\"name\": \"issueTypeName\"},\"summary\": \"summary\" , \"name1\":\"value1\", \"name0\":\"value0\" , \"components\": [{\"name\": \"component\"}] , \"labels\": [\"label0\",\"label1\"] , \"description\": \"description\"}}"));
    }
}