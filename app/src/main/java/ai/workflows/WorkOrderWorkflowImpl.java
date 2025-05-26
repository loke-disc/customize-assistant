//package ai.workflows;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.temporal.activity.ActivityOptions;
//import io.temporal.common.RetryOptions;
//import io.temporal.workflow.Workflow;
//import lombok.extern.slf4j.Slf4j;
//import platform.activities.WorkOrderActivities;
//import platform.entity.Task;
//import platform.entity.WorkOrder;
//import platform.model.TaskDefinition;
//import platform.model.WorkflowDefinition;
//import platform.service.WorkflowDefinitionParser;
//
//import java.lang.reflect.Method;
//import java.time.Duration;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//public class WorkOrderWorkflowImpl implements WorkOrderWorkflow {
//
//    ActivityOptions activityOptions = ActivityOptions.newBuilder()
//            .setTaskQueue("platform-task-queue")
//            .setStartToCloseTimeout(Duration.ofMinutes(5))
//            .setRetryOptions(
//                    RetryOptions.newBuilder()
//                            .setMaximumAttempts(3)
//                            .build())
//            .build();
//    private final WorkOrderActivities workOrderActivities = Workflow.newActivityStub(WorkOrderActivities.class, activityOptions);
////    }
//    private final WorkflowDefinitionParser workflowDefinitionParser = new WorkflowDefinitionParser();
//    private final Map<String, String> taskStatus = new HashMap<>();
//
//    private String workOrderId;
//    private String assetId;
//    private WorkOrder workOrder;
//    private final Map<String, Boolean> taskCompletionStatus = new HashMap<>();
//
//    @Override
//    public void processWorkOrder(String workOrderId, String assetId, String workflowDefinitionJSON) {
//        this.workOrderId = workOrderId;
//        this.assetId = assetId;
//
//        System.out.println("Workflow started for workOrderId - "+workOrderId);
//        log.info("Workflow started for workOrderId - "+workOrderId);
//
//        WorkflowDefinition workflowDefinition = workflowDefinitionParser.parseWorkflowDefinition(workflowDefinitionJSON);
//        this.workOrderId = workflowDefinition.getWorkflowId();
////        Map<String, String> result = workflowDefinition.getTasks().stream().collect(Collectors.toMap(TaskDefinition::getTaskId, task -> "New"));
//        List<Task> tasks = workflowDefinitionParser.extractTasks(workflowDefinitionJSON);
//
//        this.workOrder = workOrderActivities.fetchOrCreateWorkOrder(this.workOrderId, assetId, tasks);
//        //created wo
//
//
//
//        for (TaskDefinition taskDef : workflowDefinition.getTasks()) {
//            if (checkConditions(taskDef.getConditions())) {
//                executeTask(taskDef);
//                //wait for task completion signal
//                Workflow.await(() -> taskCompletionStatus.getOrDefault(taskDef.getTaskId(), false));
//                //update task status
//                this.workOrder = updateTaskStatus(this.workOrderId, taskDef.getTaskId(), "Complete");
////                task.setStatus("Complete");
////                workOrderActivities.updateWorkOrder(workOrder);
//            }
//        }
//        // Complete work order
//        workOrder.setStatus("Complete");
//        workOrderActivities.updateWorkOrderInDb(workOrder);
//    }
//
//    @Override
//    public void updateWorkOrder(String updateDetails) {
//
//    }
//
//    @Override
//    public String getStatus() {
//        return "";
//    }
//
//    public WorkOrder updateTaskStatus(String workOrderId, String taskId, String status) {
//        System.out.println("Updating workorder :"+workOrderId+" task status: "+status+" of task :"+taskId);
//        return workOrderActivities.updateTaskStatus(workOrderId, taskId, status);
//    }
//
//    private WorkflowDefinition loadWorkflowDefinition(String workflowDefinition) {
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            return objectMapper.readValue(workflowDefinition, WorkflowDefinition.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("Failed to parse workflow definition", e);
//        }
//    }
//
//    private boolean checkConditions(List<String> conditions) {
//        for (String condition: conditions) {
//            if (!taskCompletionStatus.getOrDefault(condition, false)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private void executeTask(TaskDefinition taskDef) {
//        try {
//            platform.model.ActivityOptions activityOptions = taskDef.getActivityOptions();
//            ActivityOptions temporalActivityOptions = ActivityOptions.newBuilder()
//                    .setTaskQueue(activityOptions.getTaskQueue())
//                    .setStartToCloseTimeout(activityOptions.getStartToCloseTimeoutAsDuration())
//                    .setRetryOptions(
//                            RetryOptions.newBuilder()
//                                    .setMaximumAttempts(activityOptions.getRetryOptions().getMaximumAttempts())
//                                    .build()
//                    ).build();
//            invokeActivity(taskDef.getClassName(), taskDef.getMethod(), taskDef.getTaskId(), taskDef.getServiceType(), temporalActivityOptions);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to execute tasks: " +taskDef.getTaskId(), e);
//        }
//    }
//
//    private void invokeActivity(String className, String methodName, String taskId, String serviceType, ActivityOptions temporalActivityOptions) {
//        try {
//            Class<?> activityClass = Class.forName(className);
//            Method method;
//            Object activityStub;
//            boolean status = false;
//            switch (serviceType) {
//                case "domain":
//                    method = activityClass.getMethod("executeTask", String.class, String.class);
//                    activityStub = Workflow.newActivityStub(activityClass, temporalActivityOptions);
//                    status = (boolean) method.invoke(activityStub, methodName, taskId);
//                    break;
//                case "platform":
//                    method = activityClass.getMethod(methodName, String.class);
//                    activityStub = Workflow.newActivityStub(activityClass, temporalActivityOptions);
//                    status = (boolean) method.invoke(activityStub, taskId);
//                    break;
//            }
//            taskCompletionStatus.put(taskId, status);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to invoke activity method: " + methodName, e);
//        }
//    }
//}
