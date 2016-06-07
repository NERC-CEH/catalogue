package uk.ac.ceh.gateway.catalogue.services;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createTypedLiteral;
import com.hp.hpl.jena.tdb.TDBFactory;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.spy;
import org.mockito.MockitoAnnotations;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;

/**
 *
 * @author cjohn
 */
public class JenaLookupServiceTest {
    private Dataset jenaTdb;
    private JenaLookupService service;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        jenaTdb = TDBFactory.createDataset();
        service = spy(new JenaLookupService(jenaTdb));
    }
    
    @Test
    public void checkThatCanLookupWkt() {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://doc1"), HAS_GEOMETRY, createTypedLiteral("Polygon(12,23)", WKT_LITERAL));
        
        //When
        List<String> wkt = service.wkt("http://doc1");
        
        //Then
        assertThat(wkt.size(), is(1));
        assertThat(wkt.get(0), equalTo("Polygon(12,23)"));
    }
}
