package uk.ac.ceh.gateway.catalogue.monitoring.templates;

import lombok.val;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

public class MonitoringFacilityTemplateTest {

    @Test
    void loadFacility() {
        //given
        val jenaTdb = TDBFactory.createDataset();
        val model = jenaTdb.getDefaultModel();
        val reader = new StringReader("hello");
        val base = "http://base";
        RDFDataMgr.read(model, reader, base, Lang.TTL);

        //when
        // TODO generate turtle triple statements from the freemarker template

        //then
        // TODO add assertions about some triples in the model
    }
}
