//package ai.service;
//
//import io.temporal.client.WorkflowClient;
//import io.temporal.client.WorkflowOptions;
//import io.temporal.serviceclient.WorkflowServiceStubs;
//import org.springframework.stereotype.Service;
//import platform.workflow.WorkOrderWorkflow;
//
//@Service
//public class WorkflowService {
//    private final WorkflowClient client;
//
//    public WorkflowService() {
//        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
//        this.client = WorkflowClient.newInstance(service);
//    }
//
//    public String startWorkOrder(String workOrderId, String assetId) {
//        WorkOrderWorkflow workflow = client.newWorkflowStub(WorkOrderWorkflow.class, WorkflowOptions.newBuilder().setTaskQueue("platform-task-queue").build());
//        WorkflowClient.start(workflow::processWorkOrder, workOrderId, assetId, "{}");
//        return "WorkOrder started!";
//    }
//}
