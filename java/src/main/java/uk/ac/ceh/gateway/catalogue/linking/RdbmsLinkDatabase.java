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
    
    private static final String IS_EMPTY = "select count(*) from metadata";
    private static final String TRUNCATE_METADATA = "truncate table metadata";
    private static final String TRUNCATE_COUPLED_RESOURCES = "truncate table coupledResources";
    private static final String DATASETS_FOR_SERVICE = "select d.fileIdentifier, d.title, d.resourceIdentifier, d.parentIdentifier, d.revisionOfIdentifier from metadata d inner join coupledResources c on d.resourceIdentifier = c.resourceIdentifier inner join metadata s on c.fileIdentifier = s.fileIdentifier where s.fileIdentifier = ?";
    private static final String SERVICES_FOR_DATASET = "select s.fileIdentifier, s.title, s.resourceIdentifier, s.parentIdentifier, s.revisionOfIdentifier from metadata s inner join coupledResources c on s.fileIdentifier = c.fileIdentifier inner join metadata d on c.resourceIdentifier = d.resourceIdentifier where d.fileIdentifier = ?";
    private static final String PARENT = "select p.fileIdentifier, p.title, p.resourceIdentifier, p.parentIdentifier, p.revisionOfIdentifier from metadata p inner join metadata c on c.parentIdentifier = p.fileIdentifier where c.fileIdentifier = ?";
    private static final String CHILDREN = "select fileIdentifier, title, resourceIdentifier, parentIdentifier, revisionOfIdentifier from metadata where parentIdentifier = ?";
    private static final String REVISION_OF = "select d.fileIdentifier, d.title, d.resourceIdentifier, d.parentIdentifier, d.revisionOfIdentifier from metadata d inner join metadata r on r.revisionOfIdentifier = d.fileIdentifier where r.fileIdentifier = ?";
    private static final String REVISED = "select fileIdentifier, title, resourceIdentifier, parentIdentifier, revisionOfIdentifier from metadata where revisionOfIdentifier = ?";
    private static final String INSERT_METADATA = "insert into metadata (fileIdentifier, title, resourceIdentifier, parentIdentifier, revisionOfIdentifier) values (?, ?, ?, ?, ?)";
    private static final String DELETE_METADATA = "delete from metadata where fileIdentifier = ?";
    private static final String INSERT_COUPLED_RESOURCES = "insert into coupledResources (fileIdentifier, resourceIdentifier) values (?, ?)";
    private static final String DELETE_COUPLED_RESOURCES = "delete from coupledResources where fileIdentifier = ? and resourceIdentifier = ?";
    
    @Autowired
    public RdbmsLinkDatabase(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
        rowMapper = new MetadataRowMapper();
    }
    
    @Override
    public boolean isEmpty() {
        return 0 == jdbcTemplate.queryForObject(IS_EMPTY, Integer.class);
    }

    @Override
    public void empty() {
        jdbcTemplate.execute(TRUNCATE_METADATA);
        jdbcTemplate.execute(TRUNCATE_COUPLED_RESOURCES);
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
        return query(DATASETS_FOR_SERVICE, fileIdentifier);
    }

    @Override
    public List<Metadata> findServicesForDataset(String fileIdentifier) {
        return query(SERVICES_FOR_DATASET, fileIdentifier);
    }

    @Override
    public Metadata findParent(String fileIdentifier) {
        return query(PARENT, fileIdentifier)
            .stream()
            .findFirst()
            .orElse(null);
    }

    @Override
    public List<Metadata> findChildren(String fileIdentifier) {
        return query(CHILDREN, fileIdentifier);
    }

    @Override
    public Metadata findRevised(String fileIdentifier) {
        return query(REVISED, fileIdentifier)
            .stream()
            .findFirst()
            .orElse(null);
    }

    @Override
    public Metadata findRevisionOf(String fileIdentifier) {
        return query(REVISION_OF, fileIdentifier)
            .stream()
            .findFirst()
            .orElse(null);
    }
    
    private List<Metadata> query(String query, String fileIdentifier) {
        return jdbcTemplate.query(query, rowMapper, fileIdentifier);
    }

    private class MetadataRowMapper implements RowMapper<Metadata> {
        @Override
        public Metadata mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Metadata.builder()
                .fileIdentifier(rs.getString("fileIdentifier"))
                .title(rs.getString("title"))
                .resourceIdentifier(rs.getString("resourceIdentifier"))
                .parentIdentifier(rs.getString("parentIdentifier"))
                .revisionOfIdentifier(rs.getString("revisionOfIdentifier"))
                .build();
        }
    }
    
    private void insertMetadata(List<Metadata> metadata) {
        jdbcTemplate.batchUpdate(INSERT_METADATA, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Metadata meta = metadata.get(i);
                log.debug("inserting {}", meta);
                ps.setString(1, meta.getFileIdentifier());
                ps.setString(2, meta.getTitle());
                ps.setString(3, meta.getResourceIdentifier());
                ps.setString(4, meta.getParentIdentifier());
                ps.setString(5, meta.getRevisionOfIdentifier());
            }

            @Override
            public int getBatchSize() {
                return metadata.size();
            }
        });
    }
    
    private void deleteMetadata(List<Metadata> metadata) {
        jdbcTemplate.batchUpdate(DELETE_METADATA, new BatchPreparedStatementSetter() {

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
        jdbcTemplate.batchUpdate(INSERT_COUPLED_RESOURCES, new BatchPreparedStatementSetter() {

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
        jdbcTemplate.batchUpdate(DELETE_COUPLED_RESOURCES, new BatchPreparedStatementSetter() {

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