package uk.ac.ceh.gateway.catalogue.model;

import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public class MetadataConflictExceptionTest {
    @Test
    public void carriesTheSubmittedDocument() {
        MetadataDocument doc = new GeminiDocument();
        MetadataConflictException ex = new MetadataConflictException("stale", doc);
        assertThat(ex.getMessage(), is("stale"));
        assertThat(ex.getSubmittedDocument(), is(sameInstance(doc)));
    }
}
