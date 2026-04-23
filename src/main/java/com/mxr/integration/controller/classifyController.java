package com.mxr.integration.controller;

import org.springframework.web.bind.annotation.RestController;

import com.mxr.integration.Response.MultipleProcessedResponse;
import com.mxr.integration.Response.PersonExistsResponse;
import com.mxr.integration.Response.PersonSummary;
import com.mxr.integration.Response.ProcessedResponse;
import com.mxr.integration.Response.PaginatedResponse;
import com.mxr.integration.Response.ErrorResponse;
import com.mxr.integration.model.Person;
import com.mxr.integration.request.NewEntityRequest;
import com.mxr.integration.service.IntegrationService;
import com.mxr.integration.util.NaturalLanguageQueryParser;
import com.mxr.integration.util.NaturalLanguageQueryParser.QueryFilterDTO;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class classifyController {
    private final IntegrationService integrationService;

    public classifyController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @GetMapping("/")
    public ResponseEntity<String> health() {
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @PostMapping("/api/profiles")
    public ResponseEntity<ProcessedResponse> savePerson(@Valid @RequestBody NewEntityRequest request) {
        String name = request.getName();
        ProcessedResponse response = integrationService.savePerson(name);
        if(response instanceof PersonExistsResponse) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/profiles/{id}")
    public ProcessedResponse getUserById(@PathVariable UUID id) {

        Person person = integrationService.getPersonById(id);

        return mapToProcessedResponse(person);
    }

    @GetMapping("/api/profiles")
    public MultipleProcessedResponse getUsersByParams(@RequestParam(required = false) String gender,
            @RequestParam(required = false) 
            String countryId, @RequestParam(required = false) String ageGroup) {
        List<PersonSummary> response = integrationService.searchPeople(gender, countryId, ageGroup);
        return mapSpecToMultipleProcessedResponse(response);
    }

    @DeleteMapping("/api/profiles/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable UUID id) {
        integrationService.deletePersonById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/profiles/filter")
    public ResponseEntity<PaginatedResponse<Person>> filterProfiles(
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String ageGroup,
            @RequestParam(required = false) String countryId,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) Double minGenderProbability,
            @RequestParam(required = false) Double minCountryProbability,
            @RequestParam(required = false, defaultValue = "created_at") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int limit) {

        // Validate pagination
        if (page < 1) {
            return ResponseEntity.badRequest().body(null);
        }
        if (limit < 1 || limit > 50) {
            limit = Math.min(Math.max(limit, 1), 50);
        }

        PaginatedResponse<Person> response = integrationService.filterProfiles(
                gender, ageGroup, countryId, minAge, maxAge,
                minGenderProbability, minCountryProbability,
                sortBy, order, page, limit);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/profiles/search")
    public ResponseEntity<?> searchProfiles(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int limit) {

        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("error", "Missing or empty 'q' parameter"));
        }

        QueryFilterDTO filter = NaturalLanguageQueryParser.parse(q);

        if (filter == null) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ErrorResponse("error", "Unable to interpret query"));
        }

        // Validate pagination
        if (page < 1) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("error", "Invalid page number"));
        }
        if (limit < 1 || limit > 50) {
            limit = Math.min(Math.max(limit, 1), 50);
        }

        PaginatedResponse<Person> response = integrationService.filterProfiles(
                filter.getGender(),
                filter.getAgeGroup(),
                filter.getCountryId(),
                filter.getMinAge(),
                filter.getMaxAge(),
                filter.getMinGenderProbability(),
                filter.getMinCountryProbability(),
                "created_at",
                "desc",
                page,
                limit);

        return ResponseEntity.ok(response);
    }

    private ProcessedResponse mapToProcessedResponse(Person person) {
        return ProcessedResponse.builder()
                .status("success")
                .data(person)
                .build();
    }
    

    
    private MultipleProcessedResponse mapSpecToMultipleProcessedResponse(List<PersonSummary> list) {

        return MultipleProcessedResponse.builder()
                .status("success")
                .count(list.size())
                .data(list)
                .build();
    }

}
