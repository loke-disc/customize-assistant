package ai.model;

import ai.service.DatabaseSchemaService;
import ai.service.DatabaseSchemaService.Table;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.Set;

@Data
public class McpContext {
    private final DatabaseSchemaService schemaService;
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode getFullContext(String conversationId) {
        ObjectNode context = mapper.createObjectNode();
        context.set("schema", buildSchemaContext());
        context.set("constraints", buildConstraints());
        return context;
    }

    private JsonNode buildSchemaContext() {
        ArrayNode tables = mapper.createArrayNode();
        schemaService.getTables().forEach(table -> {
            ObjectNode tableNode = mapper.createObjectNode();
            tableNode.put("name", table.getName());
            tableNode.put("description", table.getDescription());
            ArrayNode columns = mapper.createArrayNode();
            table.getColumns().forEach(column -> {
                ObjectNode columnNode = mapper.createObjectNode();
                columnNode.put("name", column.getName());
                columnNode.put("type", column.getType());
                columnNode.put("description", column.getDescription());
                columns.add(columnNode);
            });
            tableNode.set("columns", columns);
            tables.add(tableNode);
        });
        return tables;
    }

//    private JsonNode buildConstraints() {
//        ObjectNode constraints = mapper.createObjectNode();
//        constraints.put("max_execution_time_ms", 5000);
//        constraints.put("max_result_rows", 1000);
//        constraints.putArray("preferred_join_types").add("INNER_JOIN").add("LEFT_JOIN");
//        return constraints;
//    }

    public JsonNode buildWorkOrderCentricContext() {
        ObjectNode context = mapper.createObjectNode();

        // 1. Get main work_orders table
        Table workOrders = schemaService.getTableDetails("work_order");
        workOrders.setMainTable(true);

        // 2. Get all referenced tables
        Set<Table> dependentTables = schemaService.findDependentTables("work_order");

        // 3. Build schema node
        ArrayNode tables = mapper.createArrayNode();
        tables.add(convertTableToNode(workOrders));
        dependentTables.forEach(table -> tables.add(convertTableToNode(table)));

        context.set("schema", tables);
        context.set("constraints", buildConstraints());
        return context;
    }

    private JsonNode convertTableToNode(Table table) {
        ObjectNode tableNode = mapper.createObjectNode();
        tableNode.put("name", table.getName());
        tableNode.put("description", table.getDescription());
        tableNode.put("isMainTable", table.isMainTable());

        ArrayNode columns = mapper.createArrayNode();
        table.getColumns().forEach(col -> {
            ObjectNode colNode = mapper.createObjectNode();
            colNode.put("name", col.getName());
            colNode.put("type", col.getType());
            if (col.getReferences() != null) {
                colNode.put("references", col.getReferences());
            }
            columns.add(colNode);
        });

        tableNode.set("columns", columns);
        return tableNode;
    }

    private JsonNode buildConstraints() {
        ObjectNode constraints = mapper.createObjectNode();
        constraints.put("MAX_RESULT_ROWS", 50);
        constraints.put("ALLOW_ONLY_WORK_ORDER_RELATED", true);
        constraints.putArray("ALLOWED_JOIN_TYPES")
                .add("INNER JOIN")
                .add("LEFT JOIN");
        return constraints;
    }

}
