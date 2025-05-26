package ai.model.ollama;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {
    private String response;
}
