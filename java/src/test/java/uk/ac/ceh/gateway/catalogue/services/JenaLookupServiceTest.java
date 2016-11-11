package uk.ac.ceh.gateway.catalogue.services;

import java.util.Arrays;
import java.util.List;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import static org.apache.jena.rdf.model.ResourceFactory.createTypedLiteral;
import org.apache.jena.tdb.TDBFactory;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.HAS_GEOMETRY;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.IDENTIFIER;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.REFERENCES;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.SOURCE;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.TITLE;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.TYPE;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.WKT_LITERAL;
import uk.ac.ceh.gateway.catalogue.model.Link;

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
        service = new JenaLookupService(jenaTdb);
    }
    
    @Test
    public void lookupModelApplications() {
        //Given       
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://modelApplication1"), TITLE, "Model Application 1");
        triples.add(createResource("http://modelApplication1"), TYPE, "modelApplication");
        triples.add(createResource("http://modelApplication1"), REFERENCES, createResource("http://model"));
        triples.add(createResource("http://modelApplication2"), TITLE, "Model Application 2");
        triples.add(createResource("http://modelApplication2"), TYPE, "modelApplication");
        triples.add(createResource("http://modelApplication2"), REFERENCES, createResource("http://model"));
        
        //When
        List<Link> actual = service.modelApplications("http://model");
        
        //Then
        assertThat("Should be 2 Links", actual.size(), equalTo(2));
    }
    
    @Test
    public void lookupModels() {
        //Given       
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://model1"), TITLE, "Model 1");
        triples.add(createResource("http://model1"), TYPE, "model");
        triples.add(createResource("http://modelApplication"), REFERENCES, createResource("http://model1"));
        triples.add(createResource("http://model2"), TITLE, "Model 2");
        triples.add(createResource("http://model2"), TYPE, "model");
        triples.add(createResource("http://modelApplication"), REFERENCES, createResource("http://model2"));
        
        //When
        List<Link> actual = service.models("http://modelApplication");
        
        //Then
        assertThat("Should be 2 Links", actual.size(), equalTo(2));
    }
    
    @Test
    public void lookupDatasets() {
        //Given        
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://dataset1"), TITLE, "Dataset 1");
        triples.add(createResource("http://dataset1"), TYPE, "dataset");
        triples.add(createResource("http://model"), REFERENCES, createResource("http://dataset1"));
        triples.add(createResource("http://dataset2"), TITLE, "Dataset 2");
        triples.add(createResource("http://dataset2"), TYPE, "dataset");
        triples.add(createResource("http://dataset2"), REFERENCES, createResource("http://model"));
        
        //When
        List<Link> actual = service.datasets("http://model");
        
        //Then
        assertThat("Should be 2 Links", actual.size(), equalTo(2));
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
    
    @Test
    public void CanLookupLinked() {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        Resource master = createResource("http://master");
        Resource link1 = createResource("http://link1");
        Resource link2 = createResource("http://link2");
        triples.add(Arrays.asList(
            createStatement(link1, SOURCE, master),
            createStatement(link1, IDENTIFIER, createTypedLiteral("CEH:EIDC:12309843234")),
            createStatement(link1, IDENTIFIER, createTypedLiteral("doi:10.5285/049283da-ee18-4b46-b714-d76f9a1ee479")),
            createStatement(link1, IDENTIFIER, createTypedLiteral("https://catalogue.ceh.ac.uk/id/049283da-ee18-4b46-b714-d76f9a1ee479")),
            createStatement(link1, IDENTIFIER, createTypedLiteral("049283da-ee18-4b46-b714-d76f9a1ee479")),
            createStatement(link2, SOURCE, master),
            createStatement(link2, IDENTIFIER, createTypedLiteral("CEH:EIDC:9482349527435")),
            createStatement(link2, IDENTIFIER, createTypedLiteral("doi:10.5285/d8234690-1b61-4084-a349-eb53467383fe")),
            createStatement(link2, IDENTIFIER, createTypedLiteral("https://catalogue.ceh.ac.uk/id/d8234690-1b61-4084-a349-eb53467383fe9")),
            createStatement(link2, IDENTIFIER, createTypedLiteral("d8234690-1b61-4084-a349-eb53467383fe"))
        ));
        
        //When
        List<String> actual = service.linked("http://master");
        
        //Then
        assertThat("should contain two plain identifiers", actual, contains("049283da-ee18-4b46-b714-d76f9a1ee479", "d8234690-1b61-4084-a349-eb53467383fe"));
    }
}
