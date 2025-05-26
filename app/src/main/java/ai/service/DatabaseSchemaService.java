package ai.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DatabaseSchemaService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Table> getTables() {
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String tableName = rs.getString("table_name");
            Table table = new Table(tableName, "", false, new ArrayList<>(), new ArrayList<>());
            table.setColumns(getColumns(tableName));
            table.setRelationships(getRelationships(tableName));
            return table;
        });
    }

    public Table getTableDetails(String tableName) {
        Table table = new Table(tableName, "",false, new ArrayList<>(), new ArrayList<>());
        table.setColumns(getColumns(tableName));
        table.setRelationships(getRelationships(tableName));
        return table;
    }

    public Set<Table> findDependentTables(String mainTable) {
        // Find tables referenced via foreign keys FROM the main table
        String referencedTablesSql = "SELECT DISTINCT ccu.table_name AS referenced_table " +
                "FROM information_schema.table_constraints tc " +
                "JOIN information_schema.key_column_usage kcu " +
                "  ON tc.constraint_name = kcu.constraint_name " +
                "JOIN information_schema.constraint_column_usage ccu " +
                "  ON ccu.constraint_name = tc.constraint_name " +
                "WHERE tc.table_name = ? " +
                "AND tc.constraint_type = 'FOREIGN KEY'";

        Set<String> tableNames = new HashSet<>();
        tableNames.addAll(jdbcTemplate.queryForList(
                referencedTablesSql, String.class, mainTable));

        // Find tables that REFERENCE the main table
        String referencingTablesSql = "SELECT DISTINCT tc.table_name AS referencing_table " +
                "FROM information_schema.table_constraints tc " +
                "JOIN information_schema.key_column_usage kcu " +
                "  ON tc.constraint_name = kcu.constraint_name " +
                "JOIN information_schema.constraint_column_usage ccu " +
                "  ON ccu.constraint_name = tc.constraint_name " +
                "WHERE ccu.table_name = ? " +
                "AND tc.constraint_type = 'FOREIGN KEY'";

        tableNames.addAll(jdbcTemplate.queryForList(
                referencingTablesSql, String.class, mainTable));

        return tableNames.stream()
                .map(this::getTableDetails)
                .collect(Collectors.toSet());
    }

    private List<Column> getColumns(String tableName) {
        String sql = "SELECT " +
                "c.column_name, " +
                "c.data_type, " +
                "tc.constraint_type, " +
                "ccu.table_name AS references_table, " +
                "ccu.column_name AS references_column " +
                "FROM information_schema.columns c " +
                "LEFT JOIN information_schema.key_column_usage kcu " +
                "  ON c.table_name = kcu.table_name " +
                "  AND c.column_name = kcu.column_name " +
                "LEFT JOIN information_schema.table_constraints tc " +
                "  ON kcu.constraint_name = tc.constraint_name " +
                "LEFT JOIN information_schema.constraint_column_usage ccu " +
                "  ON tc.constraint_name = ccu.constraint_name " +
                "WHERE c.table_schema = 'public' " +
                "  AND c.table_name = ? " +
                "ORDER BY c.ordinal_position";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Column col = new Column(
                    rs.getString("column_name"),
                    rs.getString("data_type"),
                    "",
                    null
            );

            if ("FOREIGN KEY".equals(rs.getString("constraint_type"))) {
                col.setReferences(String.format("%s.%s",
                        rs.getString("references_table"),
                        rs.getString("references_column")));
            }

            return col;
        }, tableName);
    }

    private List<Relationship> getRelationships(String tableName) {
        String sql = "SELECT " +
                "tc.constraint_name, " +
                "kcu.column_name, " +
                "ccu.table_name AS foreign_table_name, " +
                "ccu.column_name AS foreign_column_name, " +
                "tc.constraint_type " +
                "FROM information_schema.table_constraints tc " +
                "JOIN information_schema.key_column_usage kcu " +
                "  ON tc.constraint_name = kcu.constraint_name " +
                "JOIN information_schema.constraint_column_usage ccu " +
                "  ON tc.constraint_name = ccu.constraint_name " +
                "WHERE (tc.table_name = ? OR ccu.table_name = ?) " +
                "  AND tc.constraint_type = 'FOREIGN KEY'";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Relationship(
                rs.getString("constraint_name"),
                rs.getString("column_name"),
                rs.getString("foreign_table_name"),
                rs.getString("foreign_column_name"),
                rs.getString("constraint_type")
        ), tableName, tableName);
    }

    @Data
    @AllArgsConstructor
    public static class Table {
        private String name;
        private String description;
        private boolean isMainTable;
        private List<Column> columns = new ArrayList<>();
        private List<Relationship> relationships = new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    public static class Column {
        private String name;
        private String type;
        private String description;
        private String references; // Format: "table.column"
    }

    @Data
    @AllArgsConstructor
    public static class Relationship {
        private String constraintName;
        private String columnName;
        private String foreignTableName;
        private String foreignColumnName;
        private String constraintType;
    }
}
