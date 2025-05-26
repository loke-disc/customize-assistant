package ai.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryResult {

    public enum ResponseType {
        SINGLE_VALUE, TABLE, PARAGRAPH
    }

    @JsonProperty("response_type")
    private ResponseType responseType;

    @JsonProperty("sql")
    private String sql;

    @JsonProperty("response")
    private String response;

    @JsonProperty("explanation")
    private String explanation;

    @JsonProperty("used_tables")
    private List<String> usedTables;

    @JsonCreator
    public QueryResult(
            @JsonProperty("response_type") ResponseType responseType,
            @JsonProperty("sql") String sql,
            @JsonProperty("response") String response,
            @JsonProperty("explanation") String explanation,
            @JsonProperty("used_tables") List<String> usedTables) {
        this.responseType = responseType;
        this.sql = sql;
        this.response = response;
        this.explanation = explanation;
        this.usedTables = usedTables != null ? usedTables : new ArrayList<>();
    }
}