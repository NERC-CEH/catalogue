package uk.ac.ceh.gateway.catalogue.linking;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class RdbmsLinkDatabase implements LinkDatabase {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Metadata> rowMapper;

    @Autowired
    public RdbmsLinkDatabase(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
        rowMapper = new MetadataRowMapper();
    }
    
    @Override
    public boolean isEmpty() {
        return 0 == jdbcTemplate.queryForObject("select count(*) from metadata", Integer.class);
    }

    @Override
    public void empty() {
        jdbcTemplate.execute("truncate table metadata");
        jdbcTemplate.execute("truncate table coupledResources");
    }

    @Override
    public void deleteMetadata(Metadata metadata) {
        deleteMetadata(Arrays.asList(metadata));
    }
    
    @Override
    public void deleteCoupledResources(Set<CoupledResource> coupledResources) {
        delCoupledResources(new ArrayList(coupledResources));
    }

    @Override
    public void addMetadata(Metadata metadata) {
        addMetadata(new HashSet(Arrays.asList(metadata)));
    }

    @Override
    public void addMetadata(Set<Metadata> metadata) {
        deleteMetadata(new ArrayList(metadata));
        insertMetadata(new ArrayList(metadata));
    }
    
    @Override
    public void addCoupledResources(Set<CoupledResource> coupledResources) {
        delCoupledResources(new ArrayList(coupledResources));
        insertCoupledResources(new ArrayList(coupledResources));
    }

    @Override
    public List<Metadata> findDatasetsForService(String fileIdentifier) {
        String sql = "select d.fileIdentifier, d.title, d.resourceIdentifier from metadata d inner join coupledResources c on d.resourceIdentifier = c.resourceIdentifier inner join metadata s on c.fileIdentifier = s.fileIdentifier where s.fileIdentifier = ?";
        return query(sql, fileIdentifier);
    }

    @Override
    public List<Metadata> findServicesForDataset(String fileIdentifier) {
        String sql = "select s.fileIdentifier, s.title, s.resourceIdentifier from metadata s inner join coupledResources c on s.fileIdentifier = c.fileIdentifier inner join metadata d on c.resourceIdentifier = d.resourceIdentifier where d.fileIdentifier = ?";
        return query(sql, fileIdentifier);
    }
    
    private List<Metadata> query(String query, String fileIdentifier) {
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
    
    private void insertMetadata(List<Metadata> metadata) {
        String sql = "insert into metadata (fileIdentifier, title, resourceIdentifier) values (?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Metadata meta = metadata.get(i);
                log.debug("inserting {}", meta);
                ps.setString(1, meta.getFileIdentifier());
                ps.setString(2, meta.getTitle());
                ps.setString(3, meta.getResourceIdentifier());
            }

            @Override
            public int getBatchSize() {
                return metadata.size();
            }
        });
    }
    
    private void deleteMetadata(List<Metadata> metadata) {
        String sql = "delete from metadata where fileIdentifier = ?";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Metadata meta = metadata.get(i);
                ps.setString(1, meta.getFileIdentifier());
            }

            @Override
            public int getBatchSize() {
                return metadata.size();
            }
        });
    }
    
    private void insertCoupledResources(List<CoupledResource> coupledResources) {
        String sql = "insert into coupledResources (fileIdentifier, resourceIdentifier) values (?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                CoupledResource resource = coupledResources.get(i);
                ps.setString(1, resource.getFileIdentifier());
                ps.setString(2, resource.getResourceIdentifier());
            }

            @Override
            public int getBatchSize() {
                return coupledResources.size();
            }
        });
    }
    
    private void delCoupledResources(List<CoupledResource> coupledResource) {
        String sql = "delete from coupledResources where fileIdentifier = ? and resourceIdentifier = ?";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                CoupledResource resource = coupledResource.get(i);
                ps.setString(1, resource.getFileIdentifier());
                ps.setString(2, resource.getResourceIdentifier());
            }

            @Override
            public int getBatchSize() {
                return coupledResource.size();
            }
        });
    }
}