package ai.repository;

import ai.model.SuccessfulQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SuccessfulQueryRepository extends MongoRepository<SuccessfulQuery, String> {
    Optional<SuccessfulQuery> findByNormalizedQuery(String normalizedQuery);
    @Query(value = "{'normalizedQuery': {$regex: ?0, $options: 'i'}}")
    List<SuccessfulQuery> findSimilarQueries(String queryFragment);
    void deleteByLastUsedBefore(Instant cutoff);
    void deleteByConfidenceScoreLessThanAndHitCountLessThan(double minConfidence, int minHits);
}