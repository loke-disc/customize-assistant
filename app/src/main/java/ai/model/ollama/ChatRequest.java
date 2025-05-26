package ai.model.ollama;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatRequest {
    private String model;
    private String prompt;
    private double temperature;
    private int max_tokens;
}
