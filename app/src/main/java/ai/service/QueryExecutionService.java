package ai.service;

import ai.model.QueryResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class QueryExecutionService {
    private final JdbcTemplate jdbcTemplate;
    private final NlToSqlService nlToSqlService;

    public QueryExecutionService(JdbcTemplate jdbcTemplate, NlToSqlService nlToSqlService) {
        this.jdbcTemplate = jdbcTemplate;
        this.nlToSqlService = nlToSqlService;
    }

    public Object processQuery(String conversationId, String nlQuery) {
        QueryResult queryResult = nlToSqlService.generateQuery(conversationId, nlQuery);
        switch (queryResult.getResponseType()) {
            case SINGLE_VALUE:
                return executeSingleValueQuery(queryResult.getSql());
            case TABLE:
                return executeTableQuery(queryResult.getSql());
            case PARAGRAPH:
                return queryResult.getResponse();
            default:
                throw new IllegalArgumentException("Unknown response type: " + queryResult.getResponseType());
        }
    }

    private Object executeSingleValueQuery(String sql) {
        try {
            return jdbcTemplate.queryForObject(sql, Object.class);
        } catch (Exception e) {
            return "Error retrieving single value query: " + e.getMessage();
        }
    }

    private List<Map<String, Object>> executeTableQuery(String sql) {
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            return List.of(Map.of("error", "Error retrieving table query: " + e.getMessage()));
        }
    }

    public String executeTestQuery(String workOrderId) {
        // Example query execution
        String sql = "select wos.display_name from work_order wo \n" +
                "left join work_order_status wos on wos.work_order_status_id = wo.work_order_status_id \n" +
                "where wo.work_order_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{workOrderId}, String.class);
    }
}
