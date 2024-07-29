package uk.ac.ceh.gateway.catalogue.metrics;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportMapper implements RowMapper<Map<String,String>> {
    @Override
    public Map<String, String> mapRow(ResultSet rs, int map) throws SQLException {
        LinkedHashMap<String, String> row = new LinkedHashMap<>();
        row.put("document", rs.getString("DOCUMENT"));
        row.put("doc_title", rs.getString("DOC_TITLE"));
        row.put("record_type", rs.getString("RECORD_TYPE"));
        row.put("views", String.valueOf(rs.getInt("VIEWS")));
        row.put("downloads", String.valueOf(rs.getInt("DOWNLOADS")));
        return row;
    }
}
