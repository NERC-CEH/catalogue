package uk.ac.ceh.gateway.catalogue.linking;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class RdbmsLinkDatabase implements LinkDatabase {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Metadata> rowMapper;

    public RdbmsLinkDatabase(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
        rowMapper = new MetadataRowMapper();
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
        String sql = "select d.fileIdentifier, d.title, d.resourceIdentifier from metadata d inner join coupledResources c on d.resourceIdentifier = c.resourceIdentifier inner join metadata s on c.fileIdentifier = s.fileIdentifier where s.fileIdentifier = ?";
        return query(sql, fileIdentifier);
    }

    @Override
    public Collection<Metadata> findServicesForDataset(String fileIdentifier) {
        String sql = "select s.fileIdentifier, s.title, s.resourceIdentifier from metadata s inner join coupledResources c on s.fileIdentifier = c.fileIdentifier inner join metadata d on c.resourceIdentifier = d.resourceIdentifier where d.fileIdentifier = ?";
        return query(sql, fileIdentifier);
    }
    
    private Collection<Metadata> query(String query, String fileIdentifier) {
        return jdbcTemplate.query(query, rowMapper, fileIdentifier);
    }
    
    private class MetadataRowMapper implements RowMapper<Metadata> {
        @Override
        public Metadata mapRow(ResultSet rs, int rowNum) throws SQLException {
            String fileIdentifier = rs.getString("fileIdentifier");
            String tile = rs.getString("title");
            String resourceIdentifier = rs.getString("resourceIdentifier");
            return Metadata.builder()
                .fileIdentifier(fileIdentifier)
                .title(tile)
                .resourceIdentifier(resourceIdentifier)
                .build();
        }
    }
}