package ai.repository;

import ai.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    List<Conversation> findByUpdatedAtAfter(Instant timestamp);

    Optional<Conversation> findByHistory_Query(String query);

    Optional<Conversation> findByConversationId(String conversationId);

    @Query(value = "{ 'conversationId': ?0 }", fields = "{ 'history': { $slice: ?1 } }")
    Optional<Conversation> findRecentConversationHistory(String conversationId, int limit);

    @Query(value = "{ 'conversationId': ?0 }", fields = "{ 'history': { $slice: [?1, ?2] } }")
    List<Conversation> findPagedConversationHistory(String conversationId, int skip, int limit);
}
