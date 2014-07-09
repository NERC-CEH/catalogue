package database;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.sql.DataSource;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkingException;
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
    
    @Test
    public void emptyDatabase() throws DocumentLinkingException {
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
    public void zeroResultsDoesNotCauseCrash() {
        //Given
        RdbmsLinkDatabase linkDatabase = new RdbmsLinkDatabase(createPopulatedTestDataSource());
        
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