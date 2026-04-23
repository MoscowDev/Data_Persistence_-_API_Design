package com.mxr.integration.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"status", "page", "limit", "total_elements", "data"})
public class PaginatedResponse<T> {
    private String status;
    private int page;
    private int limit;
    @JsonProperty("total_elements")
    private long total;
    private List<T> data;
}