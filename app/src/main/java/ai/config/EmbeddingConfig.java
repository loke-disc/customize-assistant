package ai.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class EmbeddingConfig {
    @Value("${mongodb.embedding.collection:embedded_schema}")
    private String collectionName;
    @Value("${mongodb.embedding.index:vector_index}")
    private String indexName;
    @Value("${rag.embedding.dimension:384}")
    private int embeddingDimension;
    // Local embedding model
    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }
    // MongoDB Vector Store
    @Bean
    public MongoDbEmbeddingStore mongoDBEmbeddingStore(MongoTemplate mongoTemplate) {
        return MongoDbEmbeddingStore.builder()
                .mongoDatabase(mongoTemplate.getDb())
                .collectionName(collectionName)
                .indexName(indexName)
                .dimension(embeddingDimension)
                .createIndex(true)
                .build();
    }
}
