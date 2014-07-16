package uk.ac.ceh.gateway.catalogue.config;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.sql.DataSource;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author cjohn
 */
public class DatabaseConfigTest {
    @Rule 
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Spy DatabaseConfig databaseConfig;
    
    @Before
    public void createDatabaseConfig() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void checkThatSchemaVersionHashIsNullIfNoFileExists() throws IOException {
        //Given
        databaseConfig.schemaHash = new File(folder.getRoot(), "doesntExist");
        
        //When
        String lastAppliedSchemaHash = databaseConfig.getMD5HexOfLastAppliedSchemaVersion();
        
        //Then
        assertNull("Expected the old schema hash to be null", lastAppliedSchemaHash);
    }
    
    @Test
    public void checkThatSchemaHashIsReadFromSchemaFile() throws IOException {
        //Given
        File schemaFile = folder.newFile();
        FileUtils.writeStringToFile(schemaFile, "my schema hash", "UTF-8");
        databaseConfig.schemaHash = schemaFile;
        
        //When
        String lastAppliedSchemaHash = databaseConfig.getMD5HexOfLastAppliedSchemaVersion();
        
        //Then
        assertEquals("Expected the to get the schema content", "my schema hash", lastAppliedSchemaHash);
    }
    
    @Test
    public void checkThatSchemaFileExists() throws URISyntaxException {
        //Given
        //Nothing
        
        //When
        File schema = databaseConfig.getSchemaFile();
        
        //Then
        assertTrue("Expected the schema to exist", schema.exists());
    }
    
    @Test
    public void checkThatDatasourceSetsSchemaIfNotPresent() throws IOException, URISyntaxException {
        //Given
        doNothing().when(databaseConfig).performSetSchemaIfNotPresent(any(JdbcTemplate.class));
        
        //When
        DataSource datasource = databaseConfig.dataSource();
        
        //Then
        ArgumentCaptor<JdbcTemplate> jdbcTemplate = ArgumentCaptor.forClass(JdbcTemplate.class);
        verify(databaseConfig).performSetSchemaIfNotPresent(jdbcTemplate.capture());
        assertSame("Expected the same datasource to be used for seting schema if not present", datasource, jdbcTemplate.getValue().getDataSource());
    }
    
    @Test
    public void checkThatSchemaIsAppliedIfSchemasDiffer() throws IOException, URISyntaxException {
        //Given
        databaseConfig.schemaHash = folder.newFile();
        
        File schema = folder.newFile();
        FileUtils.writeStringToFile(schema, "my schema", "UTF-8");
        FileUtils.writeStringToFile(databaseConfig.schemaHash, "InvalidHash", "UTF-8");
        doReturn(schema).when(databaseConfig).getSchemaFile();
        
        JdbcTemplate template = mock(JdbcTemplate.class);
        
        //When
        databaseConfig.performSetSchemaIfNotPresent(template);
        
        //Then
        String hash = FileUtils.readFileToString(databaseConfig.schemaHash, "UTF-8");
        assertEquals("Expected the hash file to be the md5 of my schema", "c1ae1a8937f1382aa2745884bd273d02", hash);
        verify(template).execute("my schema");
    }
    
    @Test
    public void checkThatSchemaIsAppliedIfNoSchemaWasAlreadySet() throws IOException, URISyntaxException {
        //Given
        databaseConfig.schemaHash = folder.newFile();
        
        File schema = folder.newFile();
        FileUtils.writeStringToFile(schema, "my schema", "UTF-8");
        doReturn(schema).when(databaseConfig).getSchemaFile();
        
        JdbcTemplate template = mock(JdbcTemplate.class);
        
        //When
        databaseConfig.performSetSchemaIfNotPresent(template);
        
        //Then
        String hash = FileUtils.readFileToString(databaseConfig.schemaHash, "UTF-8");
        assertEquals("Expected the hash file to be the md5 of my schema", "c1ae1a8937f1382aa2745884bd273d02", hash);
        verify(template).execute("my schema");
    }
    
    @Test
    public void checkThatSchemaIsNotAppliedIfSchemaIsSame() throws IOException, URISyntaxException {
        //Given
        databaseConfig.schemaHash = folder.newFile();
        
        File schema = folder.newFile();
        FileUtils.writeStringToFile(schema, "my schema", "UTF-8");
        FileUtils.writeStringToFile(databaseConfig.schemaHash, "c1ae1a8937f1382aa2745884bd273d02", "UTF-8");
        doReturn(schema).when(databaseConfig).getSchemaFile();
        
        JdbcTemplate template = mock(JdbcTemplate.class);
        
        //When
        databaseConfig.performSetSchemaIfNotPresent(template);
        
        //Then
        verifyZeroInteractions(template);
    }
}
