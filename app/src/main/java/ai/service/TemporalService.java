//package ai.service;
//
//import ai.model.WorkOrderStatus;
//import ai.workflows.WorkOrderWorkflow;
//import io.temporal.client.WorkflowClient;
//import io.temporal.client.WorkflowStub;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//public class TemporalService {
//    private final WorkflowClient workflowClient;
//
//    @Autowired
//    public TemporalService(WorkflowClient workflowClient) {
//        this.workflowClient = workflowClient;
//    }
//
//    public String getWorkflowStatus(String workOrderId) {
//        try {
//            WorkflowStub stub = workflowClient.newUntypedWorkflowStub(workOrderId);
//            return stub.query("getStatus", String.class);
//        } catch (Exception e) {
//            log.error("Error querying workflow status: {}", e.getMessage());
//            return "FAILED";
//        }
//    }
//
//    public void signalWorkOrderUpdate(String workOrderId, String updateDetails) {
//        try {
//            WorkOrderWorkflow workflow = workflowClient.newWorkflowStub(WorkOrderWorkflow.class, workOrderId);
//            workflow.updateWorkOrder(updateDetails);
//        } catch (Exception e) {
//            log.error("Error signalling workflow update: {}", e.getMessage());
//            throw new RuntimeException("Failed to signal workflow update", e);
//        }
//    }
//}
