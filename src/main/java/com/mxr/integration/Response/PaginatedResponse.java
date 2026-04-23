package com.mxr.integration.Response;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@Builder
public class PaginatedResponse<T> {
    private String status;
    private int page;
    private int limit;
    private long total;
    private List<T> data;
}

