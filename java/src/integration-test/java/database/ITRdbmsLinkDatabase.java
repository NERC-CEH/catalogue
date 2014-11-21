package database;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import uk.ac.ceh.gateway.catalogue.linking.CoupledResource;
import uk.ac.ceh.gateway.catalogue.linking.Metadata;
import uk.ac.ceh.gateway.catalogue.linking.RdbmsLinkDatabase;

public class ITRdbmsLinkDatabase {
    
    private static final String FILE_IDENTIFIER_NOT_IN_DATABASE = "234234-54323492-43289";

    private DataSource createPopulatedTestDataSource() {
        return new EmbeddedDatabaseBuilder()
			.setName("catalogue")
			.addScripts("database/schema.sql", "database/populate.sql")
			.build();
    }
    
    private DataSource createEmptyTestDataSource() {
        return new EmbeddedDatabaseBuilder()
			.setName("catalogue")
			.addScripts("database/schema.sql")
			.build();
    }
    
    @Test
    public void deleteCoupledResource() {
        //Given
        DataSource dataSource = createPopulatedTestDataSource();
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        verifyDatabaseHasRows(jdbcTemplate);
        Set<CoupledResource> toDelete = new HashSet(Arrays.asList(
            CoupledResource.builder().fileIdentifier("793d5f7a-3926-4ccf-8995-1305c7b0374c").resourceIdentifier("CEH:EIDC:#1374152631039").build()
        ));
        
        //When
        linkDatabase.deleteCoupledResources(toDelete);
        
        //Then
        Integer count = jdbcTemplate.queryForObject("select count(*) from coupledResources where fileIdentifier = '793d5f7a-3926-4ccf-8995-1305c7b0374c' and resourceIdentifier = 'CEH:EIDC:#1374152631039'", Integer.class);
        assertThat("Should not be a record with specfied fileIdentifier and resourceIdentifier", count, equalTo(0));
    }
    
    @Test
    public void addCoupledResources() {
        //Given
        DataSource dataSource = createEmptyTestDataSource();
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        verifyDatabaseIsEmpty(jdbcTemplate);
        Set<CoupledResource> resources = new HashSet(Arrays.asList(
            CoupledResource.builder().fileIdentifier("abcd").resourceIdentifier("CEH:EIDC:#0123456789").build(),
            CoupledResource.builder().fileIdentifier("abcd").resourceIdentifier("CEH:EIDC:#9876543210").build()
        ));
        
        //When
        linkDatabase.addCoupledResources(resources);
        
        //Then
        Integer count = jdbcTemplate.queryForObject("select count(*) from coupledResources", Integer.class);
        assertThat("Should be two rows in the database", count, equalTo(2));
        
    }
    
    @Test
    public void addAlreadyExisitingCoupledResources() {
        //Given
        DataSource dataSource = createPopulatedTestDataSource();
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        verifyDatabaseHasRows(jdbcTemplate);
        Set<CoupledResource> resources = new HashSet(Arrays.asList(
            CoupledResource.builder().fileIdentifier("793d5f7a-3926-4ccf-8995-1305c7b0374c").resourceIdentifier("CEH:EIDC:#1374152631039").build(),
            CoupledResource.builder().fileIdentifier("793d5f7a-3926-4ccf-8995-1305c7b0374c").resourceIdentifier("CEH:EIDC:#1395932301823").build()
        ));
        
        //When
        linkDatabase.addCoupledResources(resources);
        
        //Then
        Integer count = jdbcTemplate.queryForObject("select count(*) from coupledResources where fileIdentifier = '793d5f7a-3926-4ccf-8995-1305c7b0374c'", Integer.class);
        assertThat("Two rows should have been added", count, equalTo(2));
    }
    
    @Test
    public void addAlreadyExisitingMetadata() {
        //Given
        DataSource dataSource = createPopulatedTestDataSource();
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        verifyDatabaseHasRows(jdbcTemplate);
        Set<Metadata> metadata = new HashSet(Arrays.asList(
            Metadata.builder().fileIdentifier("793d5f7a-3926-4ccf-8995-1305c7b0374c").build()
        ));
        
        //When
        linkDatabase.addMetadata(metadata);
        
        //Then
        Integer count = jdbcTemplate.queryForObject("select count(*) from metadata where fileIdentifier = '793d5f7a-3926-4ccf-8995-1305c7b0374c'", Integer.class);
        assertThat("One row should have been added", count, equalTo(1));
    }
    
    @Test
    public void addMultipleMetadata() {
        //Given
        DataSource dataSource = createEmptyTestDataSource();
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        verifyDatabaseIsEmpty(jdbcTemplate);
        Set<Metadata> metadata = new HashSet(Arrays.asList(
            Metadata.builder().fileIdentifier("1234-4321").build(),
            Metadata.builder().fileIdentifier("abcd-dcba").build()
        ));
        
        //When
        linkDatabase.addMetadata(metadata);
        
        //Then
        Integer count = jdbcTemplate.queryForObject("select count(*) from metadata", Integer.class);
        assertThat("Two rows should have been added", count, equalTo(2));
    }
    
    @Test
    public void tryToDeleteWithEmptyString() {
        //Given
        DataSource dataSource = createPopulatedTestDataSource();
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        verifyDatabaseHasRows(jdbcTemplate);
        Integer before = jdbcTemplate.queryForObject("select count(*) from metadata", Integer.class);
        Metadata toDelete = Metadata.builder().build();
        
        //When
        linkDatabase.deleteMetadata(toDelete);
        
        //Then
        Integer after = jdbcTemplate.queryForObject("select count(*) from metadata", Integer.class);
        assertThat("Rows should not have been deleted", after, equalTo(before));
    }
    
    @Test
    public void deleteMetadataRecord() {
        //Given
        DataSource dataSource = createPopulatedTestDataSource();
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        verifyDatabaseHasRows(jdbcTemplate);
        Metadata toDelete = Metadata.builder().fileIdentifier("793d5f7a-3926-4ccf-8995-1305c7b0374c").build();
        
        //When
        linkDatabase.deleteMetadata(toDelete);
        
        //Then
        Integer count = jdbcTemplate.queryForObject("select count(*) from metadata where fileIdentifier = '793d5f7a-3926-4ccf-8995-1305c7b0374c'", Integer.class);
        assertThat("Should not be a record with specfied fileIdentifier", count, equalTo(0));
    }
    
    @Test
    public void emptyDatabase() {
        //Given
        DataSource dataSource = createPopulatedTestDataSource();
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        verifyDatabaseHasRows(jdbcTemplate);
        
        //When
        linkDatabase.empty();
        
        //Then
        verifyDatabaseIsEmpty(jdbcTemplate);
    }
    
    @Test
    public void checkMetadataRowCountOnEmptySource() {
        //Given
        DataSource dataSource = createEmptyTestDataSource();
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(dataSource);
        
        //When
        boolean isEmpty = linkDatabase.isEmpty();
        
        //Then
        assertTrue("Exepected the database to be empty", isEmpty);
    }
    
    @Test
    public void checkMetadataRowCountOnPopulatedDatasource() {
        //Given
        DataSource dataSource = createPopulatedTestDataSource();
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(dataSource);
        
        //When
        boolean isEmpty = linkDatabase.isEmpty();
        
        //Then
        assertFalse("Exepected the database to be populated", isEmpty);
    }
    
    @Test
    public void findDatasetsForService() {
        //Given
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(createPopulatedTestDataSource());
        Collection<Metadata> expected = Arrays.asList(model(), distribution());
        
        //When
        Collection<Metadata> actual = linkDatabase.findDatasetsForService("793d5f7a-3926-4ccf-8995-1305c7b0374c");       
        
        //Then
        assertThat("There should be two metadata in the collection", actual.size(), equalTo(2));
        actual.stream().forEach((metadata) -> {
            assertThat("Metadata not found in expected", metadata, isIn(expected));
        });
        
    }
    
    @Test
    public void findServicesForDataset() {
        //Given
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(createPopulatedTestDataSource());
        Collection<Metadata> expected = Arrays.asList(plyn(), another());
        
        //When
        Collection<Metadata> actual = linkDatabase.findServicesForDataset("8db84900-5fdb-43be-a607-e56c843d9b87");       
        
        //Then
        assertThat("There should be two metadata in the collection", actual.size(), equalTo(2));
        actual.stream().forEach((metadata) -> {
            assertThat("Metadata not found in expected", metadata, isIn(expected));
        });
        
    }
    
    @Test
    public void findParent() {
        //Given
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(createPopulatedTestDataSource());
        Metadata expected = Metadata.builder()
            .title("Parent Series")
            .fileIdentifier("fc77c9b3-570d-4314-82ba-bc914538a748")
            .build();
        
        //When
        Metadata actual = linkDatabase.findParent("abff8409-0995-48d2-9303-468e1a9fe3df");       
        
        //Then
        assertThat("Parent not found in expected", actual, equalTo(expected)); 
    }
    
    @Test
    public void findChildren() {
        //Given
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(createPopulatedTestDataSource());
        Collection<Metadata> expected = Arrays.asList(
            Metadata.builder()
            .title("Child Dataset 0")
            .fileIdentifier("abff8409-0995-48d2-9303-468e1a9fe3df")
            .parentIdentifier("fc77c9b3-570d-4314-82ba-bc914538a748")
            .build(),
            Metadata.builder()
            .title("Child Dataset 1")
            .fileIdentifier("dbff8409-0995-48d2-9303-468e1a9fe3dd")
            .parentIdentifier("fc77c9b3-570d-4314-82ba-bc914538a748")
            .build()
        );
        
        //When
        Collection<Metadata> actual = linkDatabase.findChildren("fc77c9b3-570d-4314-82ba-bc914538a748");       
        
        //Then
        assertThat("There should be two metadata in the collection", actual.size(), equalTo(2));
        actual.stream().forEach((metadata) -> {
            assertThat("Metadata not found in expected", metadata, isIn(expected));
        });
    }
    
    @Test
    public void zeroResultsDoesNotCauseCrash() {
        //Given
        DataSource dataSource = createPopulatedTestDataSource();
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        verifyDatabaseHasRows(jdbcTemplate);
        
        //When
        Collection<Metadata> actual = linkDatabase.findServicesForDataset(FILE_IDENTIFIER_NOT_IN_DATABASE);
        
        assertThat("actual list of Metadata should be an empty list", actual, equalTo(Collections.EMPTY_LIST)); 
    }
    
    private Metadata model() {
        return Metadata.builder()
            .title("Model estimates of topsoil moisture [Countryside Survey]")
            .fileIdentifier("8db84900-5fdb-43be-a607-e56c843d9b87")
            .resourceIdentifier("CEH:EIDC:#1395932301823")
            .build();
    }
    
    private Metadata distribution() {
        return Metadata.builder()
            .title("Distribution of ash trees in woody linear features in Great Britain")
            .fileIdentifier("05e5d538-6be7-476d-9141-76d9328738a4")
            .resourceIdentifier("CEH:EIDC:#1374152631039")
            .build();
    }
    
    private Metadata plyn() {
        return Metadata.builder()
            .title("Plynlimon research catchments map view service")
            .fileIdentifier("793d5f7a-3926-4ccf-8995-1305c7b0374c")
            .build();
    }
    
    private Metadata another() {
        return Metadata.builder()
            .title("Another service")
            .fileIdentifier("983d5f7a-3926-4ccf-8995-1305c7b0374c")
            .build();
    }
    
    private void verifyDatabaseHasRows(JdbcTemplate jdbcTemplate) {
        String sql = "select count(*) from metadata";
        Integer rows = jdbcTemplate.queryForObject(sql, Integer.class);
        assertThat("Database should be populated", rows, greaterThan(0));
    }
    
    private void verifyDatabaseIsEmpty(JdbcTemplate jdbcTemplate) {
        String sql = "select count(*) from metadata";
        Integer rows = jdbcTemplate.queryForObject(sql, Integer.class);
        assertThat("Database should be empty", rows, equalTo(0));
    }

}