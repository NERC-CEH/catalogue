package uk.ac.ceh.gateway.catalogue.config;

import javax.sql.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "uk.ac.ceh.gateway.catalogue.linking")
public class LinkConfig {
    
    @Value("${postgresql.url}") String url;
    @Value("${postgresql.username}") String username;
    @Value("${postgresql.password}") String password;
    
    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        PoolProperties p = new PoolProperties();
        p.setUrl(url);
        p.setDriverClassName("org.postgresql.Driver");
        p.setUsername(username);
        p.setPassword(password);
        p.setJmxEnabled(true);
        p.setValidationQuery("select 1");
        
        return new org.apache.tomcat.jdbc.pool.DataSource(p);
    }
}