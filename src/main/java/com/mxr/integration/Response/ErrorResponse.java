package com.mxr.integration.Response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonPropertyOrder({"status", "message"})
public class ErrorResponse {
    private String status;
    private String message;
}