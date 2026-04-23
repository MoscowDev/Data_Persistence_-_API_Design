package com.mxr.integration.Response;

import com.mxr.integration.model.Person;
import lombok.Getter;

@Getter
public class PersonExistsResponse extends ProcessedResponse {
    private final String message;

    public PersonExistsResponse(String status, Person person, String message) {
        super(status, person);
        this.message = message;
    }
}