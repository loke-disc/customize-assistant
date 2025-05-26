package ai.workflows;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface WorkOrderWorkflow {

    @WorkflowMethod
    void processWorkOrder(String workOrderId, String assetId, String workflowDefinition);

    @SignalMethod
    void updateWorkOrder(String updateDetails);

    @QueryMethod
    String getStatus();

}
