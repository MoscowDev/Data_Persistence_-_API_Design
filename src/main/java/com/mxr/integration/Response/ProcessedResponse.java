package com.mxr.integration.Response;

import com.mxr.integration.model.Person;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedResponse {
    private String status;
    private Person data;
}