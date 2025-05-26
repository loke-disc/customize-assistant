package ai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "conversations")
public class Conversation {
    @Id
    String conversationId;
    private List<Exchange> history = new ArrayList<>();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public Conversation(String conversationId) {
        this.conversationId = conversationId;
    }

    public void addExchange(String query, String response) {
        this.history.add(new Exchange(query, response, Instant.now()));
        this.updatedAt = Instant.now();
    }

    @Data
    @AllArgsConstructor
    public static class Exchange {
        private String query;
        private String response;
        private Instant timestamp = Instant.now();
    }
}
