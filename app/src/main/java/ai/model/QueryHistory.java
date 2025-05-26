package ai.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Document(collection = "query_history")
public class QueryHistory {
    @Id
    private String id;
    private String conversationId;
    private String naturalQuery;
    private String generatedSql;
    private Instant timestamp;
    // getters/setters
}
//public interface QueryHistoryRepository extends MongoRepository<QueryHistory, String> {
//    List<QueryHistory> findByConversationId(String conversationId);
//}
////@Document(collection = "successful_queries")
//public class SuccessfulQuery {
//    @Id
//    private String id;
//    private String naturalQuery;
//    private String generatedSql;
//    private Instant lastUsed;
//    // getters/setters
//}
//public interface SuccessfulQueryRepository extends MongoRepository<SuccessfulQuery, String> {
//    Optional<SuccessfulQuery> findByNaturalQuery(String naturalQuery);
//}
