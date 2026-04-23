package com.mxr.integration.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"status", "page", "limit", "total", "total_pages", "data"})
public class PaginatedResponse<T> {
    private String status;
    private int page;
    private int limit;
    @JsonProperty("total_pages")
    private int totalPages;
    private long total;
    private List<T> data;
}