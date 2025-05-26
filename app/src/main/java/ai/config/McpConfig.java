package ai.config;

import ai.model.McpContext;
import ai.repository.ConversationRepository;
import ai.service.DatabaseSchemaService;
import ai.service.McpService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public McpContext mcpContext(DatabaseSchemaService schemaService) {
        return new McpContext(schemaService);
    }


//    @Bean
//    public McpService mcpService(McpContext mcpContext, ConversationRepository conversationRepo) {
//        return new McpService(mcpContext, conversationRepo);
//    }
}
