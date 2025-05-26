package ai.repository;

import ai.model.QueryHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QueryHistoryRepository extends MongoRepository<QueryHistory, String> {
    List<QueryHistory> findByConversationId(String conversationId);
}