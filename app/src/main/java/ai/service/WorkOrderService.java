//package ai.service;
//
//import ai.model.WorkOrder;
//import ai.model.WorkOrderStatus;
//import ai.repository.WorkOrderRepository;
//import ai.workflows.WorkOrderWorkflow;
//import io.temporal.client.WorkflowClient;
//import io.temporal.client.WorkflowOptions;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.util.UUID;
//
//@Service
//@Slf4j
//public class WorkOrderService {
//    @Value("${temporal.workflow.task-queue:platform-task-queue}") private String taskQueue;
//    private final WorkflowClient workflowClient;
//    private final WorkOrderRepository workOrderRepository;
//    private final TemporalService temporalService;
//
//    @Autowired
//    public WorkOrderService(WorkflowClient workflowClient, WorkOrderRepository workOrderRepository, TemporalService temporalService) {
//        this.workflowClient = workflowClient;
//        this.workOrderRepository = workOrderRepository;
//        this.temporalService = temporalService;
//    }
//
//    public String createWorkOrder(String workflowDefinition) {
//        String workOrderId = UUID.randomUUID().toString();
//        String assetId = UUID.randomUUID().toString();
//
//        WorkflowOptions workflowOptions = WorkflowOptions.newBuilder().setTaskQueue(taskQueue).setWorkflowId(workOrderId).build();
//        WorkOrderWorkflow workflow = workflowClient.newWorkflowStub(WorkOrderWorkflow.class, workflowOptions);
//        WorkflowClient.start(workflow::processWorkOrder, workOrderId, assetId, workflowDefinition);
//        return workOrderId;
//    }
//
//    public String getWorkOrderStatus(String workOrderId) {
//        WorkOrder workOrder = workOrderRepository.findByWorkOrderId(workOrderId).orElseThrow(() -> new RuntimeException("WorkOrder not found!"));
//        String temporalStatus = temporalService.getWorkflowStatus(workOrderId);
//        if (!temporalStatus.equalsIgnoreCase(workOrder.getStatus())) {
//            workOrder.setStatus(temporalStatus);
//            workOrderRepository.save(workOrder);
//        }
//        return workOrder.getStatus();
//    }
//
//    public String updateWorkOrder(String workOrderId, String updateDetails) {
////        WorkOrder workOrder = workOrderRepository.findByWorkOrderId(workOrderId).orElseThrow(() -> new RuntimeException("WorkOrder not found!"));
////        temporalService.signalWorkOrderUpdate(workOrderId, updateDetails);
////        workOrder.
//        return "Update done!";
//    }
//}
