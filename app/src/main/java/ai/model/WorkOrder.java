//package ai.model;
//
//import lombok.Data;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;
//import platform.entity.Task;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Document(collection = "work_orders")
//@Data
//@RequiredArgsConstructor
//public class WorkOrder {
//    @Id
//    private String workOrderId;
//    private String assetId;
//    private LocalDateTime createdAt;
//    private LocalDateTime lastModifiedAt;
//    private String status;
//    private List<Task> tasks;
//}
