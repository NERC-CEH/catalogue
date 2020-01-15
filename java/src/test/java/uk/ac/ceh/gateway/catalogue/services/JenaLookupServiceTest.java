package uk.ac.ceh.gateway.catalogue.services;

import java.util.Arrays;
import java.util.List;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import static org.apache.jena.rdf.model.ResourceFactory.createTypedLiteral;

import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.tdb.TDBFactory;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.model.Link;


public class JenaLookupServiceTest {
    private Dataset jenaTdb;
    private JenaLookupService service;

    private static final Property OSDP_PRODUCES = ResourceFactory.createProperty("http://onto.nerc.ac.uk/CEHMD/rels/produces");
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        jenaTdb = TDBFactory.createDataset();
        service = new JenaLookupService(jenaTdb);
    }
/*  Presumably these need re-writing but I don't understand them
    @Test
    public void lookupRelationships() {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://dataset1"), TITLE, "Dataset 1");
        triples.add(createResource("http://monitoringActivity"), OSDP_PRODUCES, createResource("http://dataset1"));

        //When
        List<Link> actual = service.relationships("http://monitoringActivity", OSDP_PRODUCES.toString());

        //Then
        assertThat("Should be 1 Link", actual.size(), equalTo(1));
        assertThat("Tile should be Dataset 1", actual.stream().findFirst().get().getTitle(), equalTo("Dataset 1"));
    }

    @Test
    public void lookupInverseRelationships() {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://monitoringActivity"), TITLE, "Monitoring Activity");
        triples.add(createResource("http://monitoringActivity"), OSDP_PRODUCES, createResource("http://dataset1"));

        //When
        List<Link> actual = service.inverseRelationships("http://dataset1", OSDP_PRODUCES.toString());

        //Then
        assertThat("Should be 1 Link", actual.size(), equalTo(1));
        assertThat("Tile should be Dataset 1", actual.stream().findFirst().get().getTitle(), equalTo("Monitoring Activity"));
    }
    
    @Test
    public void lookupMetadata() {
        //Given 
        String id = "7e1c18b2-ff78-4979-9a90-f7ae20b9d75b";
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://model"), TITLE, "Model");
        triples.add(createResource("http://model"), IDENTIFIER, id);
        
        //When
        Link actual = service.metadata(id);
        
        //Then
        assertThat("Should be link present", actual, is(notNullValue()));
        assertThat("title should be equal", actual.getTitle(), is("Model"));
        assertThat("href should be equal", actual.getHref(), is("http://model"));
    }
    
    @Test
    public void lookupNonExistantMetadata() {
        //Given 
        String id = "7e1c18b2-ff78-4979-9a90-f7ae20b9d75b";
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://model"), TITLE, "Model");
        triples.add(createResource("http://model"), IDENTIFIER, id);
        
        //When
        Link actual = service.metadata("a different id");
        
        //Then
        assertThat("Should not be link present", actual, is(nullValue()));
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
    public void lookupLinkDatasets() {
        //Given        
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://link1"), TITLE, "Link 1");
        triples.add(createResource("http://link1"), SOURCE, createResource("http://dataset1"));
        triples.add(createResource("http://model"), REFERENCES, createResource("http://link1"));
        triples.add(createResource("http://dataset1"), TITLE, "Dataset 1");
        triples.add(createResource("http://dataset1"), TYPE, "dataset");
        
        //When
        List<Link> actual = service.datasets("http://model");
        
        //Then
        assertThat("Should be 1 Link", actual.size(), equalTo(1));
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
    } */
}
