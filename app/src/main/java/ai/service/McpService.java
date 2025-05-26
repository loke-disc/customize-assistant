package ai.service;

import ai.model.Conversation;
import ai.model.McpContext;
import ai.repository.ConversationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class McpService {
    private final McpContext mcpContext;
    private final ConversationRepository conversationRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public McpService(McpContext mcpContext, ConversationRepository conversationRepo) {
        this.mcpContext = mcpContext;
        this.conversationRepo = conversationRepo;
    }

    public JsonNode getEnhancedContext(String conversationId, String newQuery) {
        Conversation conversation = conversationRepo.findById(conversationId)
                .orElse(new Conversation(conversationId));
        ObjectNode context = (ObjectNode) mcpContext.buildWorkOrderCentricContext();
        //add convo history
        ArrayNode history = (ArrayNode) context.putArray("conversation_history");
        conversation.getHistory().forEach(item -> history.add(item.getQuery()));
        //add curr query context
        ObjectNode currentQuery = context.putObject("current_query");
        currentQuery.put("text", newQuery);
        currentQuery.put("timestamp", Instant.now().toString());
        return context;
    }

//    public void updateConversation(String conversationId, String query, String response) {
//        Conversation conversation = conversationRepo.findById(conversationId)
//                .orElse(new Conversation(conversationId));
//        conversation.addExchange(query, response);
//        conversationRepo.save(conversation);
//    }

    public JsonNode getWorkOrderCentricContext(String conversationId, String newQuery) {
        // Start with work-order focused schema
        ObjectNode context = (ObjectNode) mcpContext.buildWorkOrderCentricContext();

        // Add conversation history if available
        if (conversationId != null) {
            Conversation conversation = conversationRepo.findById(conversationId)
                    .orElse(new Conversation(conversationId));

            ArrayNode history = context.putArray("conversation_history");
            conversation.getHistory().forEach(exchange -> {
                ObjectNode exchangeNode = mapper.createObjectNode();
                exchangeNode.put("query", exchange.getQuery());
                exchangeNode.put("response", exchange.getResponse());
                exchangeNode.put("timestamp", exchange.getTimestamp().toString());
                history.add(exchangeNode);
            });
        }

        // Add current query context
        ObjectNode currentQuery = context.putObject("current_query");
        currentQuery.put("text", newQuery);
        currentQuery.put("timestamp", Instant.now().toString());
        currentQuery.put("isWorkOrderRelated", true);

        return context;
    }

    public void updateConversation(String conversationId, String query, String response) {
        Conversation conversation = conversationRepo.findById(conversationId)
                .orElse(new Conversation(conversationId));

        conversation.addExchange(query, response);
        conversationRepo.save(conversation);
    }
}
