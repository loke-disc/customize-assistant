package ai.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "successful_queries")
@Data
@CompoundIndexes({
        @CompoundIndex(name = "natural_query_idx", def = "{'normalizedQuery': 1}", unique = true),
})
public class SuccessfulQuery {
    @Id
    private String id;
    @TextIndexed
    private String naturalQuery;
    @Indexed
    private String normalizedQuery;
    private String generatedSql;
    @Indexed(name = "ttl_idx", expireAfterSeconds = 2_592_000)
    private Instant lastUsed;
    private int hitCount = 0;
    private double confidenceScore = 0.9;
    public SuccessfulQuery(String naturalQuery, String normalizedQuery,
                           String generatedSql, Instant lastUsed, int hitCount) {
        this.naturalQuery = naturalQuery;
        this.normalizedQuery = normalizedQuery;
        this.generatedSql = generatedSql;
        this.lastUsed = lastUsed;
        this.hitCount = hitCount;
    }
}
