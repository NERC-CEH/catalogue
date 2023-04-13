package uk.ac.ceh.gateway.catalogue.elter;

import lombok.val;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.gemini.RelatedRecord;
import uk.ac.ceh.gateway.catalogue.model.Relationship;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class ElterDocumentTest {
    private final String rel1 = "https://example.com/rel/1";
    private final String doc1 = "https://example.com/doc/1";
    private final String doc2 = "https://example.com/doc/2";
    private final String doc3 = "https://example.com/doc/3";
    @Test
    void relationshipsFromRelatedRecords() {
        // given
        val expected = Sets.newHashSet(
            new Relationship(rel1, doc1),
            new Relationship(rel1, doc2),
            new Relationship(rel1, doc3)
        );
        val document = new ElterDocument();
        document.setRelatedRecords(List.of(
            new RelatedRecord(rel1, "1", doc1, "", ""),
            new RelatedRecord(rel1, "1", doc2, "", ""),
            new RelatedRecord(rel1, "1", doc3, "", "")
        ));

        // when
        val actual = document.getRelationships();

        // then
        assertThat(actual, equalTo(expected));
    }

    @Test
    void relationshipsFromRelationships() {
        // given
        val expected = Sets.newHashSet(
            new Relationship(rel1, doc1),
            new Relationship(rel1, doc2),
            new Relationship(rel1, doc3)
        );
        val document = new ElterDocument();
        document.setRelationships(Set.of(
            new Relationship(rel1, doc1),
            new Relationship(rel1, doc2),
            new Relationship(rel1, doc3)
        ));

        // when
        val actual = document.getRelationships();

        // then
        assertThat(actual, equalTo(expected));
    }

    @Test
    void relationshipsFromBoth() {
        // given
        val expected = Sets.newHashSet(
            new Relationship(rel1, doc1),
            new Relationship(rel1, doc2),
            new Relationship(rel1, doc3)
        );
        val document = new ElterDocument();
        document.setRelatedRecords(List.of(
            new RelatedRecord(rel1, "1", doc1, "", "")
        ));
        document.setRelationships(Set.of(
            new Relationship(rel1, doc2),
            new Relationship(rel1, doc3)
        ));

        // when
        val actual = document.getRelationships();

        // then
        assertThat(actual, equalTo(expected));
    }

    @Test
    void relationshipsFromRelatedRecordsNonePopulated() {
        // given
        val expected = Sets.newHashSet();
        val document = new ElterDocument();

        // when
        val actual = document.getRelationships();

        // then
        assertThat(actual, equalTo(expected));
    }

}
