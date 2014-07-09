package uk.ac.ceh.gateway.catalogue.linking;

import java.util.Arrays;
import java.util.Collection;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class RdbmsLinkDatabase implements LinkDatabase {
    private final JdbcTemplate jdbcTemplate;

    public RdbmsLinkDatabase(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void empty() throws DocumentLinkingException {
        jdbcTemplate.execute("TRUNCATE TABLE metadata");
        jdbcTemplate.execute("TRUNCATE TABLE coupledResources");
    }

    @Override
    public void delete(Metadata metadata) throws DocumentLinkingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(Metadata metadata) throws DocumentLinkingException {
        add(Arrays.asList(metadata));
    }

    @Override
    public void add(Collection<Metadata> metadata) throws DocumentLinkingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Metadata> findDatasetsForService(String fileIdentifier) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Metadata> findServicesForDataset(String fileIdentifier) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}