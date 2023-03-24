package uk.ac.ceh.gateway.catalogue.gemini;

import lombok.val;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.model.Relationship;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class RelatedRecordTest {
    private final String rel = "https://example.com/related";
    private final String href = "https://example.com/document/1";

    @Test
    void relationshipsFromRelatedRecord() {
        // given
        val relatedRecord = new RelatedRecord(rel,"1", href, "title", "something");
        val expected = new Relationship(rel, href);

        // when
        val actual = relatedRecord.toRelationship().get();

        // then
        assertThat(actual, equalTo(expected));
    }

}
