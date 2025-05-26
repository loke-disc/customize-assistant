//package ai.config;
//
//import com.slack.api.bolt.App;
//import com.slack.api.bolt.AppConfig;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class SlackConfig {
//
//    @Value("${slack.signing-secret}")
//    private String signingSecret;
//
//    @Value("${slack.bot-token}")
//    private String botToken;
//
//    @Value("${slack.app-token}")
//    private String appToken;
//
//    @Bean
//    public AppConfig slackAppConfig() {
//        return AppConfig.builder().signingSecret(signingSecret).singleTeamBotToken(botToken).build();
//    }
//
//    @Bean
//    public App slackApp(AppConfig appConfig) {
//        return new App(appConfig);
//    }
//}
