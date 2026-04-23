package com.mxr.integration.Response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({"status", "count", "data"})
public class MultipleProcessedResponse {
    private String status;
    private int count;
    private List<PersonSummary> data;
}