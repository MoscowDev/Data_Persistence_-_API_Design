package com.mxr.integration.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.mxr.integration.Response.MultipleProcessedResponse;
import com.mxr.integration.Response.PaginatedResponse;
import com.mxr.integration.Response.PersonExistsResponse;
import com.mxr.integration.Response.PersonSummary;
import com.mxr.integration.Response.ProcessedResponse;
import com.mxr.integration.exceptions.PersonNotFoundException;
import com.mxr.integration.model.Person;
import com.mxr.integration.request.NewEntityRequest;
import com.mxr.integration.service.IntegrationService;

import jakarta.validation.Valid;

@RestController
public class classifyController {

    private final IntegrationService integrationService;

    public classifyController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @GetMapping("/")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/api/profiles")
    public ResponseEntity<ProcessedResponse> savePerson(@Valid @RequestBody NewEntityRequest request) {
        if (integrationService.getRepo().existsByName(request.getName())) {
            Person person = integrationService.getRepo().findByNameIgnoreCase(request.getName())
                    .orElseThrow(() -> new PersonNotFoundException("Person not found"));
            ProcessedResponse response = ProcessedResponse.builder()
                    .status("success")
                    .data(person)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        ProcessedResponse response = integrationService.savePerson(request.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/profiles/{id}")
    public ProcessedResponse getUserById(@PathVariable UUID id) {
        Person person = integrationService.getPersonById(id);
        return ProcessedResponse.builder()
                .status("success")
                .data(person)
                .build();
    }

    @GetMapping("/api/profiles")
    public MultipleProcessedResponse getUsersByParams(@RequestParam(required = false) String gender,
                                                      @RequestParam(required = false, name = "country_id") String countryId,
                                                      @RequestParam(required = false, name = "age_group") String ageGroup) {
        List<PersonSummary> response = integrationService.searchPeople(gender, countryId, ageGroup);
        return MultipleProcessedResponse.builder()
                .status("success")
                .count(response.size())
                .data(response)
                .build();
    }

    @DeleteMapping("/api/profiles/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable UUID id) {
        integrationService.deletePersonById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/profiles/filter")
    public ResponseEntity<PaginatedResponse<Person>> filterProfiles(
            @RequestParam(required = false) String gender,
            @RequestParam(required = false, name = "age_group") String age_group,
            @RequestParam(required = false, name = "country_id") String country_id,
            @RequestParam(required = false, name = "min_age") Integer min_age,
            @RequestParam(required = false, name = "max_age") Integer max_age,
            @RequestParam(required = false, name = "min_gender_probability") Double min_gender_probability,
            @RequestParam(required = false, name = "min_country_probability") Double min_country_probability,
            @RequestParam(required = false, name = "sort_by", defaultValue = "created_at") String sort_by,
            @RequestParam(required = false, defaultValue = "desc") String order,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "5") int limit) {

        return ResponseEntity.ok(integrationService.filterProfiles(
                gender, age_group, country_id, min_age, max_age,
                min_gender_probability, min_country_probability,
                sort_by, order, page, limit));
    }

    @GetMapping("/api/profiles/search")
    public ResponseEntity<PaginatedResponse<Person>> searchProfiles(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "5") int limit) {

        return ResponseEntity.ok(integrationService.searchProfilesByQuery(q, page, integrationService.getEffectiveLimit(limit)));
    }
}