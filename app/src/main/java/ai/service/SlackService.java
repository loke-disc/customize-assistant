//package ai.service;
//
//import com.slack.api.bolt.App;
//import com.slack.api.bolt.context.builtin.SlashCommandContext;
//import com.slack.api.bolt.response.Response;
//import com.slack.api.methods.response.chat.ChatPostMessageResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.concurrent.CompletableFuture;
//
//@Service
//@Slf4j
//public class SlackService {
//    private final App slackApp;
//    private final AIService aiService;
//    private final WorkOrderService workOrderService;
//
//    @Autowired
//    public SlackService(App slackApp, AIService aiService, WorkOrderService workOrderService) {
//        this.slackApp = slackApp;
//        this.aiService = aiService;
//        this.workOrderService = workOrderService;
//        configureSlackCommands();
//    }
//
//    private void configureSlackCommands() {
//        slackApp.command("/create-workorder", (req, ctx) -> {
//            String text = req.getPayload().getText();
//            return handleCreateWorkOrderCommand(text, ctx);
//        });
//        slackApp.command("/workorder-status", (req, ctx) -> {
//            String workOrderId = req.getPayload().getText();
//            return handleWorkOrderStatusCommand(workOrderId, ctx);
//        });
//        slackApp.command("/update-workorder", (req, ctx) -> {
//            String[] parts = req.getPayload().getText().split(" ", 2);
//            if (parts.length < 2) {
//                return ctx.ack("Usage: /update-workorder <workorder-id> <update-details>");
//            }
//            return handleUpdateWorkOrderCommand(parts[0], parts[1], ctx);
//        });
//        slackApp.command("/customize-ai", (req, ctx) -> {
//            String question = req.getPayload().getText();
//            return handleAIChatCommand(question, ctx);
//        });
//    }
//
//    private Response handleCreateWorkOrderCommand(String workflowDefinition, SlashCommandContext ctx) {
//        CompletableFuture.runAsync(() -> {
//            try {
//                String initialResponse = "Creating workorder based on your request...";
//                ctx.respond(initialResponse);
//                String workOrderId = workOrderService.createWorkOrder(workflowDefinition);
//                String response = String.format("Workorder created successfully! ID: %s", workOrderId);
//                ctx.respond(response);
//            } catch (Exception e) {
//                try {
//                    ctx.respond("Error creating workorder: " +e.getMessage());
//                } catch (IOException ex) {
//                    throw new RuntimeException(ex);
//                }
//            }
//        });
//        return ctx.ack();
//    }
//
//    private Response handleWorkOrderStatusCommand(String workOrderId, SlashCommandContext ctx) {
//        CompletableFuture.runAsync(() -> {
//            try {
//                String status = workOrderService.getWorkOrderStatus(workOrderId);
//                ctx.respond("Workorder status: "+status);
//            } catch (Exception e) {
//                try {
//                    ctx.respond("Error fetching workorder status: " +e.getMessage());
//                } catch (IOException ex) {
//                    throw new RuntimeException(ex);
//                }
//            }
//        });
//        return ctx.ack();
//    }
//
//    private Response handleUpdateWorkOrderCommand(String workOrderId, String updateDetails, SlashCommandContext ctx) {
//        CompletableFuture.runAsync(() -> {
//            try {
//                String result = workOrderService.updateWorkOrder(workOrderId, updateDetails);
//                ctx.respond(result);
//            } catch (Exception e) {
//                try {
//                    ctx.respond("Error updating workorder: " +e.getMessage());
//                } catch (IOException ex) {
//                    throw new RuntimeException(ex);
//                }
//            }
//        });
//        return ctx.ack();
//    }
//
//    private Response handleAIChatCommand(String question, SlashCommandContext ctx) {
//        CompletableFuture.runAsync(() -> {
//            try {
//                String initialResponse = "Thinking about your question...";
//                ctx.respond(initialResponse);
//                String aiResponse = aiService.chatWithLLM(question).block();
//                ctx.respond(aiResponse);
//            } catch (Exception e) {
//                try {
//                    ctx.respond("Error processing your question: " +e.getMessage());
//                } catch (IOException ex) {
//                    throw new RuntimeException(ex);
//                }
//            }
//        });
//        return ctx.ack();
//    }
//
//    public ChatPostMessageResponse sendMessage(String channelId, String message) {
//        try {
//            return slackApp.client().chatPostMessage(r -> r.channel(channelId).text(message));
//        } catch (Exception e) {
//            log.error("Error sending Slack message: {}", e.getMessage());
//            throw new RuntimeException("Failed to send Slack message", e);
//        }
//    }
//
//}
