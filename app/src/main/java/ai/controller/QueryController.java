package ai.controller;

import ai.service.QueryExecutionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assistant/query")
public class QueryController {
    private final QueryExecutionService queryService;

    public QueryController(QueryExecutionService queryExecutionService) {
        this.queryService = queryExecutionService;
    }

    @PostMapping("/{conversationId}")
    public ResponseEntity<?> executeQuery(@PathVariable String conversationId,
                                          @RequestBody String nlQuery) {
        Object result = queryService.processQuery(conversationId, nlQuery);
        HttpHeaders headers = new HttpHeaders();
        if (result instanceof String) {
            headers.setContentType(MediaType.TEXT_PLAIN);
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    @PostMapping("/test/{workOrderId}")
    public ResponseEntity<?> executeTestQuery(@PathVariable String workOrderId) {
        String result = queryService.executeTestQuery(workOrderId);
        return ResponseEntity.ok(result);
    }
}
