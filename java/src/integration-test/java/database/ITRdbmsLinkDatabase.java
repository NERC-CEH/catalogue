package database;

import javax.sql.DataSource;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import uk.ac.ceh.gateway.catalogue.linking.DocumentLinkingException;
import uk.ac.ceh.gateway.catalogue.linking.RdbmsLinkDatabase;

public class ITRdbmsLinkDatabase {
    
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