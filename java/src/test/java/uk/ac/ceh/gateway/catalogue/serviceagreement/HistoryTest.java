package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.val;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.serviceagreement.History.Revision;
import uk.ac.ceh.gateway.catalogue.serviceagreement.ServiceAgreementControllerTest.TestRevision;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class HistoryTest {

    @Test
    void versionsInCorrectOrder() {
        //given
        val baseUri = "https://example.com/";
        val id = "cac082bc-cd2f-48fb-988e-425f99364b77";
        List<DataRevision<CatalogueUser>> revisions = List.of(
            new TestRevision("4"),
            new TestRevision("3"),
            new TestRevision("2"),
            new TestRevision("1")
        );

        //when
        val history = new History(baseUri, id, revisions);

        //then
        assertThat(history.getHistoryOf(), equalTo(id));
        assertThat(history.getRevisions(), containsInRelativeOrder(
            equalTo(new Revision("3", baseUri, id, "3")),
            equalTo(new Revision("2", baseUri, id, "2")),
            equalTo(new Revision("1", baseUri, id, "1"))
        ));
        assertThat(history.getRevisions(), not(hasItem(new Revision("4", baseUri, id, "4"))));
    }

}
