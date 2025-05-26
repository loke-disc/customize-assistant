package ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingInitializationService {
    private final EmbeddingModel embeddingModel;
    private final MongoDBEmbeddingStore embeddingStore;
    private final CachedMcpContext mcpService;
    @PostConstruct
    public void initializeEmbeddings() {
        JsonNode schema = mcpService.getWorkOrderCentricContext(null, null);
        List<TextSegment> segments = convertSchemaToSegments(schema);
        // Generate and store embeddings
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);
    }
    private List<TextSegment> convertSchemaToSegments(JsonNode schema) {
        List<TextSegment> segments = new ArrayList<>();
        schema.get("schema").forEach(table -> {
            String tableName = table.get("name").asText();
            // Table-level segment
            segments.add(TextSegment.from(
                    String.format("Table %s: %s", tableName, table.get("description").asText()),
                    Metadata.metadata("tableName", tableName)
            ));
            // Column-level segments
            table.get("columns").forEach(column -> {
                String columnName = column.get("name").asText();
                segments.add(TextSegment.from(
                        String.format("Column %s.%s: %s (%s)",
                                tableName,
                                columnName,
                                column.get("description").asText(),
                                column.get("type").asText()),
                        Metadata.metadata()
                                .put("tableName", tableName)
                                .put("columnName", columnName)
                                .put("type", column.get("type").asText())
                                .build()
                ));
            });
        });
        return segments;
    }
}
