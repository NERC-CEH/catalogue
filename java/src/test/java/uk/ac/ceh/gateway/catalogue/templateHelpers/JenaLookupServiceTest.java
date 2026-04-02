package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.val;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.tdb1.TDB1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ceh.gateway.catalogue.model.Link;

import java.util.Arrays;
import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology.*;

public class JenaLookupServiceTest {
    private Dataset jenaTdb;
    private JenaLookupService service;

    private static final Property DCTERMS_SOURCE = ResourceFactory.createProperty("http://purl.org/dc/terms/source");
    private static final Property DCTERMS_ISPARTOF = ResourceFactory.createProperty("http://purl.org/dc/terms/isPartOf");
    private static final Property SKOS_NARROWER = ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#narrower");

    @BeforeEach
    void init() {
        jenaTdb = TDB1Factory.createDataset();
        service = new JenaLookupService(jenaTdb);
    }

    @Test
    void findEidcRelationsForCollection() {
        //given
        val collection = "https://collection";
        val dataset1 = "http://dataset1";
        val dataset2 = "http://dataset2";
        val other = "http://other";
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource(dataset1), DCTERMS_TITLE, "Dataset 1");
        triples.add(createResource(dataset1), METADATA_STATUS, "published");
        triples.add(createResource(dataset1), DCTERMS_TYPE, "dataset");
        triples.add(createResource(dataset1), DCTERMS_ISPARTOF, createResource(collection));
        triples.add(createResource(dataset2), DCTERMS_TITLE, "Dataset 2");
        triples.add(createResource(dataset2), METADATA_STATUS, "published");
        triples.add(createResource(dataset2), DCTERMS_TYPE, "dataset");
        triples.add(createResource(dataset2), DCTERMS_ISPARTOF, createResource(collection));
        triples.add(createResource(other), DCTERMS_REFERENCES, createResource(collection));
        triples.add(createResource(other), METADATA_STATUS, "published");

        //when
        List<Link> actual = service.incomingEidcRelations(collection);

        //then
        assertThat(actual.size(), equalTo(2));

    }

    @Test
    public void lookupRelationships() {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://dataset1"), DCTERMS_TITLE, "Dataset 1");
        triples.add(createResource("http://dataset1"), METADATA_STATUS, "published");
        triples.add(createResource("http://dataset1"), DCTERMS_TYPE, "dataset");
        triples.add(createResource("http://monitoringActivity"), DCTERMS_SOURCE, createResource("http://dataset1"));

        //When
        List<Link> actual = service.relationships("http://monitoringActivity", DCTERMS_SOURCE.toString());

        //Then
        assertThat("Should be 1 Link", actual.size(), equalTo(1));
        assertThat("Tile should be Dataset 1", actual.stream().findFirst().orElseThrow().getTitle(), equalTo("Dataset 1"));
    }

    @Test
    public void lookupInverseRelationships() {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://monitoringActivity"), DCTERMS_TITLE, "Monitoring Activity");
        triples.add(createResource("http://monitoringActivity"), METADATA_STATUS, "published");
        triples.add(createResource("http://monitoringActivity"), DCTERMS_SOURCE, createResource("http://dataset1"));
        triples.add(createResource("http://monitoringActivity"), DCTERMS_TYPE, "dataset");

        //When
        List<Link> actual = service.inverseRelationships("http://dataset1", DCTERMS_SOURCE.toString());

        //Then
        assertThat("Should be 1 Link", actual.size(), equalTo(1));
        assertThat("Tile should be Dataset 1", actual.stream().findFirst().orElseThrow().getTitle(), equalTo("Monitoring Activity"));
    }

    @Test
    public void lookupInverseRelationshipsWithGeometries() {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        String geometryString = "{\"type\":\"Feature\",\"properties\":{\"name\":\"Sample Point\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0,0]}}";
        triples.add(createResource("http://monitoringFacility"), DCTERMS_TITLE, "Monitoring Facility");
        triples.add(createResource("http://monitoringFacility"), METADATA_STATUS, "published");
        triples.add(createResource("http://monitoringFacility"), DCTERMS_ISPARTOF, createResource("http://network1"));
        triples.add(createResource("http://monitoringFacility"), DCTERMS_TYPE, "Monitoring Facility");
        triples.add(createResource("http://monitoringFacility"), SF_GEOMETRY, geometryString);

        //When
        List<Link> actual = service.inverseRelationships("http://network1", DCTERMS_ISPARTOF.toString());

        //Then
        assertThat("Should be 1 Link", actual.size(), equalTo(1));
        assertThat("Tile should be Monitoring Facility", actual.stream().findFirst().orElseThrow().getTitle(), equalTo("Monitoring Facility"));
        assertThat("Geometry should be resolved", actual.stream().findFirst().orElseThrow().getGeometry(), equalTo(geometryString));
    }

    @Test
    public void inverseRelationshipCombinedGeometries() throws JsonProcessingException {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        String geometryString = "{\"type\":\"Feature\",\"properties\":{\"name\":\"Sample Point\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0,0]}}";
        String geometryString2 = "{\"type\":\"Feature\",\"properties\":{\"name\":\"Sample Point2\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,1]}}";
        String combinedGeometry = "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"name\":\"Sample Point\",\"title\":\"Monitoring Facility\",\"link\":\"http://monitoringFacility\",\"availability\":\"Inactive\",\"locationConfidential\":false},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0,0]}},{\"type\":\"Feature\",\"properties\":{\"name\":\"Sample Point2\",\"title\":\"Monitoring Facility 2\",\"link\":\"http://monitoringFacility2\",\"availability\":\"\",\"locationConfidential\":false},\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,1]}}]}";
        triples.add(createResource("http://monitoringFacility"), DCTERMS_TITLE, "Monitoring Facility");
        triples.add(createResource("http://monitoringFacility"), METADATA_STATUS, "published");
        triples.add(createResource("http://monitoringFacility"), DCTERMS_ISPARTOF, createResource("http://network1"));
        triples.add(createResource("http://monitoringFacility"), DCTERMS_TYPE, "Monitoring Facility");
        triples.add(createResource("http://monitoringFacility"), SF_GEOMETRY, geometryString);
        triples.add(createResource("http://monitoringFacility"), DOO_OPERATIONALSTATUS, "Inactive");
        triples.add(createResource("http://monitoringFacility2"), DCTERMS_TITLE, "Monitoring Facility 2");
        triples.add(createResource("http://monitoringFacility2"), METADATA_STATUS, "published");
        triples.add(createResource("http://monitoringFacility2"), DCTERMS_ISPARTOF, createResource("http://network1"));
        triples.add(createResource("http://monitoringFacility2"), DCTERMS_TYPE, "Monitoring Facility");
        triples.add(createResource("http://monitoringFacility2"), SF_GEOMETRY, geometryString2);

        //When
        String actual = service.inverseRelationshipCombinedGeometries("http://network1", DCTERMS_ISPARTOF.toString());

        //Then
        assertThat("Generates correct combined GeoJSON", actual, equalTo(combinedGeometry));
    }

    @Test
    public void relationshipCombinedGeometriesWithOwner() throws JsonProcessingException {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        String geometryString = "{\"type\":\"Feature\",\"properties\":{\"name\":\"Sample Point\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0,0]}}";
        String geometryString2 = "{\"type\":\"Feature\",\"properties\":{\"name\":\"Sample Point2\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,1]}}";
        String combinedGeometry = "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"name\":\"Sample Point\",\"title\":\"Monitoring Facility\",\"link\":\"http://monitoringFacility\",\"availability\":\"Inactive\",\"locationConfidential\":false},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0,0]}},{\"type\":\"Feature\",\"properties\":{\"name\":\"Sample Point2\",\"title\":\"Monitoring Facility 2\",\"showPolygon\":true,\"availability\":\"\",\"locationConfidential\":false},\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,1]}}]}";
        triples.add(createResource("http://monitoringFacility"), DCTERMS_TITLE, "Monitoring Facility");
        triples.add(createResource("http://monitoringFacility"), METADATA_STATUS, "published");
        triples.add(createResource("http://monitoringFacility"), DCTERMS_TYPE, "Monitoring Facility");
        triples.add(createResource("http://monitoringFacility"), SF_GEOMETRY, geometryString);
        triples.add(createResource("http://monitoringFacility"), DOO_OPERATIONALSTATUS, "Inactive");
        triples.add(createResource("http://monitoringFacility2"), DCTERMS_TITLE, "Monitoring Facility 2");
        triples.add(createResource("http://monitoringFacility2"), METADATA_STATUS, "published");
        triples.add(createResource("http://monitoringFacility2"), SKOS_NARROWER, createResource("http://monitoringFacility"));
        triples.add(createResource("http://monitoringFacility2"), DCTERMS_TYPE, "Monitoring Facility");
        triples.add(createResource("http://monitoringFacility2"), SF_GEOMETRY, geometryString2);

        //When
        String actual = service.relationshipCombinedGeometriesWithOwner("http://monitoringFacility2", SKOS_NARROWER.toString(), false);

        //Then
        assertThat("Generates correct combined GeoJSON with owner", actual, equalTo(combinedGeometry));
    }

    @Test
    public void lookupProgrammeFeatures() {
        // Given
        Model triples = jenaTdb.getDefaultModel();
        String geometryString = "{\"type\":\"Feature\",\"properties\":{\"name\":\"Facility 1\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0,0]}}";
        String geometryString2 = "{\"type\":\"Feature\",\"properties\":{\"name\":\"Facility 2\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,1]}}";
        triples.add(createResource("http://facility1"), DCTERMS_TITLE, "Facility 1");
        triples.add(createResource("http://facility1"), METADATA_STATUS, "published");
        triples.add(createResource("http://facility1"), DCTERMS_TYPE, "facility");
        triples.add(createResource("http://facility1"), SF_GEOMETRY, geometryString);
        triples.add(createResource("http://facility2"), DCTERMS_TITLE, "Facility 2");
        triples.add(createResource("http://facility2"), METADATA_STATUS, "published");
        triples.add(createResource("http://facility2"), DCTERMS_TYPE, "facility");
        triples.add(createResource("http://facility2"), SF_GEOMETRY, geometryString2);
        triples.add(createResource("http://programme"), createProperty("https://digital.ceh.ac.uk/ontology/doo/utilises"), createResource("http://facility1"));
        triples.add(createResource("http://programme"), createProperty("https://digital.ceh.ac.uk/ontology/doo/utilises"), createResource("http://facility2"));

        // When
        List<Link> actual = service.programmeFeatures("http://programme");

        // Then
        assertThat("Should be 2 Links", actual.size(), equalTo(2));
        assertThat("First title should be Facility 1", actual.get(0).getTitle(), equalTo("Facility 1"));
        assertThat("Second title should be Facility 2", actual.get(1).getTitle(), equalTo("Facility 2"));
    }

    @Test
    public void programmeCombinedGeometries() throws JsonProcessingException {
        // Given
        Model triples = jenaTdb.getDefaultModel();
        String geometryString = "{\"type\":\"Feature\",\"properties\":{\"name\":\"Facility 1\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0,0]}}";
        String geometryString2 = "{\"type\":\"Feature\",\"properties\":{\"name\":\"Facility 2\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,1]}}";
        String combinedGeometry = "{\"type\":\"FeatureCollection\",\"features\":["
            + "{\"type\":\"Feature\",\"properties\":{\"name\":\"Facility 1\",\"title\":\"Facility 1\",\"link\":\"http://facility1\",\"availability\":\"Inactive\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0,0]}},"
            + "{\"type\":\"Feature\",\"properties\":{\"name\":\"Facility 2\",\"title\":\"Facility 2\",\"link\":\"http://facility2\",\"availability\":\"\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,1]}}"
            + "]}";
        triples.add(createResource("http://facility1"), DCTERMS_TITLE, "Facility 1");
        triples.add(createResource("http://facility1"), METADATA_STATUS, "published");
        triples.add(createResource("http://facility1"), DCTERMS_TYPE, "facility");
        triples.add(createResource("http://facility1"), SF_GEOMETRY, geometryString);
        triples.add(createResource("http://facility1"), DOO_OPERATIONALSTATUS, "Inactive");
        triples.add(createResource("http://facility2"), DCTERMS_TITLE, "Facility 2");
        triples.add(createResource("http://facility2"), METADATA_STATUS, "published");
        triples.add(createResource("http://facility2"), DCTERMS_TYPE, "facility");
        triples.add(createResource("http://facility2"), SF_GEOMETRY, geometryString2);
        triples.add(createResource("http://programme"), createProperty("https://digital.ceh.ac.uk/ontology/doo/utilises"), createResource("http://facility1"));
        triples.add(createResource("http://programme"), createProperty("https://digital.ceh.ac.uk/ontology/doo/utilises"), createResource("http://facility2"));

        // When
        String actual = service.programmeCombinedGeometries("http://programme");

        // Then
        assertThat("Generates correct combined GeoJSON", actual, equalTo(combinedGeometry));
    }

    @Test
    public void lookupMetadata() {
        //Given
        String id = "7e1c18b2-ff78-4979-9a90-f7ae20b9d75b";
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://model"), DCTERMS_TITLE, "Model");
        triples.add(createResource("http://model"), METADATA_STATUS, "published");
        triples.add(createResource("http://model"), DCTERMS_IDENTIFIER, id);
        triples.add(createResource("http://model"), DCTERMS_TYPE, "dataset");

        //When
        Link actual = service.metadata(id);

        //Then
        assertThat("Should be link present", actual, is(notNullValue()));
        assertThat("title should be equal", actual.getTitle(), is("Model"));
        assertThat("href should be equal", actual.getHref(), is("http://model"));
    }

    @Test
    public void lookupNonExistentMetadata() {
        //Given
        String id = "7e1c18b2-ff78-4979-9a90-f7ae20b9d75b";
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://model"), DCTERMS_TITLE, "Model");
        triples.add(createResource("http://model"), METADATA_STATUS, "published");
        triples.add(createResource("http://model"), DCTERMS_IDENTIFIER, id);

        //When
        Link actual = service.metadata("a different id");

        //Then
        assertThat("Should not be link present", actual, is(nullValue()));
    }

    @Test
    public void lookupModelApplications() {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://modelApplication1"), DCTERMS_TITLE, "Model Application 1");
        triples.add(createResource("http://modelApplication1"), METADATA_STATUS, "published");
        triples.add(createResource("http://modelApplication1"), DCTERMS_TYPE, "modelApplication");
        triples.add(createResource("http://modelApplication1"), DCTERMS_REFERENCES, createResource("http://model"));
        triples.add(createResource("http://modelApplication2"), DCTERMS_TITLE, "Model Application 2");
        triples.add(createResource("http://modelApplication2"), METADATA_STATUS, "published");
        triples.add(createResource("http://modelApplication2"), DCTERMS_TYPE, "modelApplication");
        triples.add(createResource("http://modelApplication2"), DCTERMS_REFERENCES, createResource("http://model"));

        //When
        List<Link> actual = service.modelApplications("http://model");

        //Then
        assertThat("Should be 2 Links", actual.size(), equalTo(2));
    }

    @Test
    public void lookupModels() {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://model1"), DCTERMS_TITLE, "Model 1");
        triples.add(createResource("http://model1"), METADATA_STATUS, "published");
        triples.add(createResource("http://model1"), DCTERMS_TYPE, "model");
        triples.add(createResource("http://modelApplication"), DCTERMS_REFERENCES, createResource("http://model1"));
        triples.add(createResource("http://model2"), DCTERMS_TITLE, "Model 2");
        triples.add(createResource("http://model2"), METADATA_STATUS, "published");
        triples.add(createResource("http://model2"), DCTERMS_TYPE, "model");
        triples.add(createResource("http://modelApplication"), DCTERMS_REFERENCES, createResource("http://model2"));

        //When
        List<Link> actual = service.models("http://modelApplication");

        //Then
        assertThat("Should be 2 Links", actual.size(), equalTo(2));
    }

    @Test
    public void lookupDatasets() {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://dataset1"), DCTERMS_TITLE, "Dataset 1");
        triples.add(createResource("http://dataset1"), DCTERMS_TYPE, "dataset");
        triples.add(createResource("http://dataset1"), METADATA_STATUS, "published");
        triples.add(createResource("http://model"), DCTERMS_TITLE, "Model 1");
        triples.add(createResource("http://model"), DCTERMS_TYPE, "model");
        triples.add(createResource("http://model"), METADATA_STATUS, "published");
        triples.add(createResource("http://dataset2"), DCTERMS_TITLE, "Dataset 2");
        triples.add(createResource("http://dataset2"), DCTERMS_TYPE, "dataset");
        triples.add(createResource("http://dataset2"), METADATA_STATUS, "published");
        triples.add(createResource("http://model"), DCTERMS_REFERENCES, createResource("http://dataset1"));
        triples.add(createResource("http://dataset2"), DCTERMS_REFERENCES, createResource("http://model"));

        //When
        List<Link> actual = service.datasets("http://model");

        //Then
        assertThat("Should be 2 Links", actual.size(), equalTo(2));
    }

    @Test
    public void lookupLinkDatasets() {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://link1"), DCTERMS_TITLE, "Link 1");
        triples.add(createResource("http://link1"), METADATA_STATUS, "published");
        triples.add(createResource("http://link1"), DCTERMS_SOURCE, createResource("http://dataset1"));
        triples.add(createResource("http://model"), DCTERMS_REFERENCES, createResource("http://link1"));
        triples.add(createResource("http://model"), METADATA_STATUS, "published");
        triples.add(createResource("http://dataset1"), DCTERMS_TITLE, "Dataset 1");
        triples.add(createResource("http://dataset1"), METADATA_STATUS, "published");
        triples.add(createResource("http://dataset1"), DCTERMS_TYPE, "dataset");

        //When
        List<Link> actual = service.datasets("http://model");

        //Then
        assertThat("Should be 1 Link", actual.size(), equalTo(1));
    }

    @Test
    public void checkThatCanLookupWkt() {
        //Given
        Model triples = jenaTdb.getDefaultModel();
        triples.add(createResource("http://doc1"), SF_GEOMETRY, createTypedLiteral("Polygon(12,23)", WKT_LITERAL));

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
            createStatement(link1, DCTERMS_SOURCE, master),
            createStatement(link1, DCTERMS_IDENTIFIER, createTypedLiteral("CEH:EIDC:12309843234")),
            createStatement(link1, DCTERMS_IDENTIFIER, createTypedLiteral("doi:10.5285/049283da-ee18-4b46-b714-d76f9a1ee479")),
            createStatement(link1, DCTERMS_IDENTIFIER, createTypedLiteral("https://catalogue.ceh.ac.uk/id/049283da-ee18-4b46-b714-d76f9a1ee479")),
            createStatement(link1, DCTERMS_IDENTIFIER, createTypedLiteral("049283da-ee18-4b46-b714-d76f9a1ee479")),
            createStatement(link2, DCTERMS_SOURCE, master),
            createStatement(link2, DCTERMS_IDENTIFIER, createTypedLiteral("CEH:EIDC:9482349527435")),
            createStatement(link2, DCTERMS_IDENTIFIER, createTypedLiteral("doi:10.5285/d8234690-1b61-4084-a349-eb53467383fe")),
            createStatement(link2, DCTERMS_IDENTIFIER, createTypedLiteral("https://catalogue.ceh.ac.uk/id/d8234690-1b61-4084-a349-eb53467383fe9")),
            createStatement(link2, DCTERMS_IDENTIFIER, createTypedLiteral("d8234690-1b61-4084-a349-eb53467383fe"))
        ));

        //When
        List<String> actual = service.linked("http://master");

        //Then
        assertTrue(actual.contains("049283da-ee18-4b46-b714-d76f9a1ee479"));
        assertTrue(actual.contains("d8234690-1b61-4084-a349-eb53467383fe"));

    }
}
