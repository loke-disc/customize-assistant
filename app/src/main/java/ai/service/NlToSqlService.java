package ai.service;

import ai.model.Conversation;
import ai.model.QueryResult;
import ai.model.SuccessfulQuery;
import ai.repository.ConversationRepository;
import ai.repository.SuccessfulQueryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NlToSqlService {
    private final ChatLanguageModel chatModel;
    private final McpService mcpService;
    private final CachedMcpContext cachedMcpContext;
    private final RagService ragService;
    private final ConversationRepository conversationRepo;
    private final SuccessfulQueryRepository successfulQueryRepo;
    private final JdbcTemplate jdbcTemplate;

    public NlToSqlService(@Qualifier("sqlOptimizedModel") ChatLanguageModel chatModel, McpService mcpService, CachedMcpContext cachedMcpContext,
                          RagService ragService, ConversationRepository conversationRepo,
                          SuccessfulQueryRepository successfulQueryRepo, JdbcTemplate jdbcTemplate) {
        this.chatModel = chatModel;
        this.mcpService = mcpService;
        this.cachedMcpContext = cachedMcpContext;
        this.ragService = ragService;
        this.conversationRepo = conversationRepo;
        this.successfulQueryRepo = successfulQueryRepo;
        this.jdbcTemplate = jdbcTemplate;
    }

    public QueryResult generateQuery(String conversationId, String nlQuery) {
        try {
            //get mcp context
            JsonNode mcpContext = cachedMcpContext.getWorkOrderCentricContext(conversationId, nlQuery);
            //generate prompt with mcp context
            String prompt = buildPromptV1(mcpContext, nlQuery);
            log.info("Prompt: {}", prompt);
            //get response from chat model
            String llmResponse = chatModel.generate(prompt);
            log.info("LLM Response: {}", llmResponse);
            //parse response
            QueryResult result = parseResponse(llmResponse);
            //update conversation history
            mcpService.updateConversation(conversationId, nlQuery, result.getResponse());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate SQL query: ", e);
        }
    }

    private String buildPromptV1(JsonNode mcpContext, String query) {
        // Build the prompt using the mcpContext and nlQuery
        return String.format(
                "You are an expert PostgreSQL generator with access to this Model Context Protocol schema:\n" +
                        "%s\n" +
                        "For this query: \"%s\"\n" +
                        "Task:\n" +
                        "Convert the natural language request into a valid PostgreSQL SQL query."+
                        "Generate a JSON response with:\n" +
                        "{\n" +
                        "  \"response_type\": \"SINGLE_VALUE|TABLE|PARAGRAPH\",\n" +
                        "  \"sql\": \"SELECT...\", // only for SINGLE_VALUE or TABLE\n" +
                        "  \"response\": \"...\" // only for PARAGRAPH\n" +
                        "  \"explanation\": \"...\" // optional\n" +
                        "}\n" +
                        "Rules:\n" +
                        "      1. Apply the table_name, exact column_name from the given model context protocol\n" +
                        "      2. Apply the value and generate an executable sql in the json response\n" +
                        "      3. Use the most efficient query possible\n" +
                        "      4. Respect all MCP constraints\n" +
                        "      5. Consider conversation history\n" +
                        "      6. For follow-up questions, reference previous queries\n",
                mcpContext.toPrettyString(), query
        );
    }

//    private QueryResult parseResponse(String llmResponse) {
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            return objectMapper.readValue(llmResponse, QueryResult.class);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to parse LLM response: ", e);
//        }
//    }

    private QueryResult parseResponse(String llmResponse) {
        try {
            // 1. Extract JSON from potentially verbose LLM response
            String jsonString = extractJsonFromResponse(llmResponse);

            // 2. Parse the clean JSON
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, QueryResult.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LLM response: " + e.getMessage(), e);
        }
    }

    private String extractJsonFromResponse(String llmResponse) {
        // Pattern to match JSON objects (including those with nested structures)
        Pattern jsonPattern = Pattern.compile("\\{(?:[^{}]|\\{(?:[^{}]|\\{[^{}]*\\})*\\})*\\}");
        Matcher matcher = jsonPattern.matcher(llmResponse);

        if (matcher.find()) {
            String potentialJson = matcher.group(0);

            // Validate it's properly formatted JSON
            if (isValidJson(potentialJson)) {
                return potentialJson;
            }
        }

        // Try extracting from markdown code blocks
        String[] codeBlocks = llmResponse.split("```");
        if (codeBlocks.length >= 2) {
            for (int i = 1; i < codeBlocks.length; i += 2) {
                String blockContent = codeBlocks[i].replaceFirst("(?i)json\\s*", "").trim();
                if (isValidJson(blockContent)) {
                    return blockContent;
                }
            }
        }

        throw new IllegalArgumentException("No valid JSON found in LLM response");
    }

    private boolean isValidJson(String json) {
        try {
            new ObjectMapper().readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public QueryResult processQuery(String conversationId, String naturalQuery) {
        // 1. Check cache first
        Optional<SuccessfulQuery> cached = successfulQueryRepo.findByNaturalQuery(naturalQuery);
        if (cached.isPresent()) {
            return new QueryResult(
                    cached.get().getGeneratedSql(),
                    QueryResult.ResponseType.TABLE,
                    Collections.emptyList()
            );
        }

        // 2. Get conversation history (last 5 exchanges)
        List<Conversation.Exchange> history = conversationRepo.findRecentConversationHistory(conversationId, 5)
                .map(Conversation::getHistory)
                .orElse(Collections.emptyList());

        // 3. Retrieve relevant schema via RAG
        List<TextSegment> schemaContext = ragService.retrieveRelevantSchema(naturalQuery);

        // 4. Build prompt with history context
        String prompt = buildPrompt(naturalQuery, schemaContext, history);

        // 5. Generate SQL
        String llmResponse = chatModel.generate(prompt);
        QueryResult result = parseResponse(llmResponse);

        // 6. Validate and execute
        validateAndExecute(result.getSql());

        // 7. Update conversation history
        updateConversation(conversationId, naturalQuery, result);

        // 8. Cache successful queries
        cacheSuccessfulQuery(naturalQuery, result.getSql());

        return result;
    }

    private String buildPrompt(String query, List<TextSegment> schemaContext,
                               List<Conversation.Exchange> history) {
        String historyContext = history.stream()
                .map(ex -> String.format("User: %s\nSystem: %s", ex.getUserQuery(), ex.getSystemResponse()))
                .collect(Collectors.joining("\n\n"));

        return String.format(
                "Database Schema Context:\n" +
                        "%s\n\n" +
                        "Conversation History:\n" +
                        "%s\n\n" +
                        "New User Query:\n" +
                        "%s\n\n" +
                        "Generate SQL following these rules:\n" +
                        "1. Use EXACT table/column names from schema\n" +
                        "2. Consider previous questions and answers\n" +
                        "3. For follow-ups, maintain consistency\n" +
                        "4. Include LIMIT 100 unless specified\n" +
                        "5. Return JSON format:\n" +
                        "{\n" +
                        "  \"sql\": \"...\",\n" +
                        "  \"response_type\": \"TABLE|SINGLE_VALUE|PARAGRAPH\"\n" +
                        "}\n",
                schemaContext.stream().map(TextSegment::text).collect(Collectors.joining("\n")),
                historyContext,
                query
        );
    }

    private void updateConversation(String conversationId, String query, QueryResult result) {
        Conversation conversation = conversationRepo.findByConversationId(conversationId)
                .orElse(new Conversation(conversationId));

        String response = result.getResponseType() == QueryResult.ResponseType.PARAGRAPH
                ? result.getResponse()
                : result.getSql();

        conversation.addExchange(query, response);
        conversationRepo.save(conversation);
    }

    private void validateAndExecute(String sql) {
        try {
            jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            throw new RuntimeException("Invalid SQL: " + e.getMessage());
        }
    }

    private void cacheSuccessfulQuery(String naturalQuery, String generatedSql) {
        try {
            String normalizedQuery = normalizeQuery(naturalQuery);

            Optional<SuccessfulQuery> existing = successfulQueryRepo.findByNormalizedQuery(normalizedQuery);

            if (existing.isPresent()) {
                SuccessfulQuery cached = existing.get();
                cached.setLastUsed(Instant.now());
                cached.setHitCount(cached.getHitCount() + 1);

                if (isMoreOptimized(generatedSql, cached.getGeneratedSql())) {
                    cached.setGeneratedSql(generatedSql);
                }

                successfulQueryRepo.save(cached);
            } else {
                SuccessfulQuery newEntry = new SuccessfulQuery(
                        naturalQuery,
                        normalizedQuery,
                        generatedSql,
                        Instant.now(),
                        1
                );

                double confidence = calculateConfidence(generatedSql);
                newEntry.setConfidenceScore(confidence);

                successfulQueryRepo.save(newEntry);
            }

            if (ThreadLocalRandom.current().nextInt(0, 100) < 5) {
                cleanupOldCacheEntries();
            }
        } catch (Exception e) {
            log.error("Failed to cache successful query", e);
        }
    }

    private String normalizeQuery(String naturalQuery) {
        return naturalQuery.trim()
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .replaceAll("[^a-z0-9\\s]", "");
    }

    private boolean isMoreOptimized(String newSql, String existingSql) {
        return !newSql.contains("SELECT *") && existingSql.contains("SELECT *");
    }

    private double calculateConfidence(String sql) {
        try {
            List<Map<String, Object>> explain = jdbcTemplate.queryForList("EXPLAIN " + sql);
            return explain.isEmpty() ? 0.9 : 1.0;
        } catch (Exception e) {
            return 0.7;
        }
    }

    @Scheduled(fixedRate = 86_400_000)
    private void cleanupOldCacheEntries() {
        Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);
        successfulQueryRepo.deleteByLastUsedBefore(cutoff);
        successfulQueryRepo.deleteByConfidenceScoreLessThanAndHitCountLessThan(0.6, 3);
    }
}
