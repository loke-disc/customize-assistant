package ai.service;

import ai.model.Conversation;
import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RagService {
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final CachedMcpContext mcpService;

    public RagService(EmbeddingModel embeddingModel,
                      CachedMcpContext mcpService) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = new InMemoryEmbeddingStore<>();
        this.mcpService = mcpService;
        initializeEmbeddings();
    }

    private void initializeEmbeddings() {
        JsonNode schema = mcpService.getWorkOrderCentricContext(null, null);
        List<TextSegment> segments = extractSchemaSegments(schema);
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);
    }

    private List<TextSegment> extractSchemaSegments(JsonNode schema) {
        List<TextSegment> segments = new ArrayList<>();

        schema.get("schema").forEach(table -> {
            String tableDesc = String.format("Table %s: %s",
                    table.get("name").asText(),
                    table.get("description").asText());

            segments.add(TextSegment.from(tableDesc));

            table.get("columns").forEach(column -> {
                String colDesc = String.format("Column %s.%s: %s (%s)",
                        table.get("name").asText(),
                        column.get("name").asText(),
                        column.get("description").asText(),
                        column.get("type").asText());

                segments.add(TextSegment.from(colDesc));
            });
        });

        return segments;
    }

    public List<EmbeddingMatch<TextSegment>> retrieveRelevantSchema(String query) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        return embeddingStore.findRelevant(queryEmbedding, 5);
    }

    public List<TextSegment> retrieveRelevantSchema(String query, List<Conversation.Exchange> history) {
        // Combine current query with historical context
        String fullContext = history.stream()
                .map(ex -> ex.getUserQuery() + " " + ex.getSystemResponse())
                .collect(Collectors.joining(" ")) + " " + query;
        Embedding queryEmbedding = embeddingModel.embed(fullContext).content();
        return embeddingStore.findRelevant(queryEmbedding, 5);
    }
}
