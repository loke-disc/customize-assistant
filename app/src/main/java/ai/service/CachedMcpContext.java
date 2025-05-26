package ai.service;

import ai.model.Conversation;
import ai.repository.ConversationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.time.Instant;

@Service
public class CachedMcpContext {
    private final JsonNode workOrdersContext;
    private final ObjectMapper mapper;
    private final ConversationRepository conversationRepo;

    public CachedMcpContext(
            @Value("classpath:mcp-configs/work-orders-context.json") Resource resource,
            ConversationRepository conversationRepo) throws IOException {
        this.mapper = new ObjectMapper();
        this.workOrdersContext = mapper.readTree(resource.getInputStream());
        this.conversationRepo = conversationRepo;
    }

    public JsonNode getWorkOrderCentricContext(String conversationId, String newQuery) {
        ObjectNode context = workOrdersContext.deepCopy();

        if (conversationId != null) {
            addConversationHistory(context, conversationId);
        }

        addCurrentQuery(context, newQuery);
        return context;
    }

    public void updateConversation(String conversationId, String query, String response) {
        Conversation conversation = conversationRepo.findById(conversationId)
                .orElse(new Conversation(conversationId));

        conversation.addExchange(query, response);
        conversationRepo.save(conversation);
    }

    private void addConversationHistory(ObjectNode context, String conversationId) {
        conversationRepo.findById(conversationId).ifPresent(conversation -> {
            ArrayNode history = mapper.createArrayNode();
            conversation.getHistory().forEach(exchange -> {
                ObjectNode exchangeNode = mapper.createObjectNode();
                exchangeNode.put("query", exchange.getQuery());
                exchangeNode.put("response", exchange.getResponse());
                exchangeNode.put("timestamp", exchange.getTimestamp().toString());
                history.add(exchangeNode);
            });
            context.set("conversation_history", history);
        });
    }

    private void addCurrentQuery(ObjectNode context, String newQuery) {
        ObjectNode currentQuery = mapper.createObjectNode();
        currentQuery.put("text", newQuery);
        currentQuery.put("timestamp", Instant.now().toString());
        currentQuery.put("isWorkOrderRelated", true);
        context.set("current_query", currentQuery);
    }
}
