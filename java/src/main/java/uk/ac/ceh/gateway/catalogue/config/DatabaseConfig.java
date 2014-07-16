package uk.ac.ceh.gateway.catalogue.config;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.sql.DataSource;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseConfig {
    private static final String SCHEMA_ENCODING = "UTF-8";
    
    @Value("${postgresql.url}") String url;
    @Value("${postgresql.username}") String username;
    @Value("${postgresql.password}") String password;
    @Value("${postgresql.schemaHash}") File schemaHash;
    
    @Bean(destroyMethod = "close")
    public DataSource dataSource() throws IOException, URISyntaxException {
        PoolProperties p = new PoolProperties();
        p.setUrl(url);
        p.setDriverClassName("org.postgresql.Driver");
        p.setUsername(username);
        p.setPassword(password);
        p.setJmxEnabled(true);
        p.setValidationQuery("select 1");
        
        DataSource toReturn = new org.apache.tomcat.jdbc.pool.DataSource(p);
        performSetSchemaIfNotPresent(new JdbcTemplate(toReturn));
        return toReturn;
    }
    
    protected void performSetSchemaIfNotPresent(JdbcTemplate jdbcTemplate) throws IOException, URISyntaxException {
        //Get the schema generation file
        String sqlSchema = FileUtils.readFileToString(getSchemaFile(), SCHEMA_ENCODING);
        
        //Get the md5 hash of the schema as it is now
        String md5Hex = DigestUtils.md5Hex(sqlSchema);
        
        if(!md5Hex.equals(getMD5HexOfLastAppliedSchemaVersion())) {
            FileUtils.writeStringToFile(schemaHash, md5Hex, SCHEMA_ENCODING);
            jdbcTemplate.execute(sqlSchema); //Execute the schema
        }
    }
    
    protected String getMD5HexOfLastAppliedSchemaVersion() throws IOException {
        //Check file on disk for schema version currently applied
        if(schemaHash.exists()) {
            return FileUtils.readFileToString(schemaHash, SCHEMA_ENCODING);
        }
        else {
            return null;
        }
    }
    
    protected File getSchemaFile() throws URISyntaxException {
        return new File(DatabaseConfig.class.getResource("/db-schema.sql").toURI());
    }
}