//package ai.service;
//
//import ai.model.ollama.ChatRequest;
//import ai.model.ollama.ChatResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//@Service
//@Slf4j
//public class AIService {
//    private final WebClient webClient;
//    private final String model;
//    private final double temperature;
//    private final int maxTokens;
//
//    @Autowired
//    public AIService(WebClient webClient,
//                     @Value("${ollama.model}") String model,
//                     @Value("${ollama.temperature}") double temperature,
//                     @Value("${ollama.max-token}") int maxTokens) {
//        this.webClient = webClient;
//        this.model = model;
//        this.temperature = temperature;
//        this.maxTokens = maxTokens;
//    }
//
//    public Mono<String> chatWithLLM(String prompt) {
//        ChatRequest request = new ChatRequest(model, prompt, temperature, maxTokens);
//        return webClient.post()
//                .uri("/api/chat")
//                .bodyValue(request)
//                .retrieve()
//                .bodyToMono(ChatResponse.class)
//                .map(ChatResponse::getResponse)
//                .doOnError(error -> log.error("Error communicating with Ollama: {}", error.getMessage()));
//
//    }
//}
