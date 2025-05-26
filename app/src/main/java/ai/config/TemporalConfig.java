//package ai.config;
//
//import io.temporal.client.WorkflowClient;
//import io.temporal.serviceclient.WorkflowServiceStubs;
//import io.temporal.worker.Worker;
//import io.temporal.worker.WorkerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import platform.activities.PlatformActivities;
//import platform.activities.WorkOrderActivities;
//import platform.workflow.WorkOrderWorkflowImpl;
//
//@Configuration
//public class TemporalConfig {
//    @Bean
//    public WorkflowServiceStubs workflowServiceStubs() {
//        return WorkflowServiceStubs.newLocalServiceStubs();
//    }
//
//    @Bean
//    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
//        return WorkflowClient.newInstance(serviceStubs);
//    }
//
//    @Bean
//    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
//        return WorkerFactory.newInstance(workflowClient);
//    }
//
//    @Bean
//    public Worker platformWorker(WorkerFactory workerFactory, WorkOrderActivities workOrderActivities, PlatformActivities platformActivities) {
//        Worker worker = workerFactory.newWorker("platform-task-queue");
//        worker.registerWorkflowImplementationTypes(WorkOrderWorkflowImpl.class);
//        worker.registerActivitiesImplementations(workOrderActivities, platformActivities);
//        workerFactory.start();
//        return worker;
//    }
//}
