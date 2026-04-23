package com.mxr.integration.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewEntityRequest {

    @NotBlank(message = "name is required")
    private String name;
}