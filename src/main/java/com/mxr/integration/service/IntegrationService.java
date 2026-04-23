package com.mxr.integration.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mxr.integration.Response.AgifyResponse;
import com.mxr.integration.Response.GenderizeResponse;
import com.mxr.integration.Response.NationalizeResponse;
import com.mxr.integration.Response.PaginatedResponse;
import com.mxr.integration.Response.PersonExistsResponse;
import com.mxr.integration.Response.MultipleProcessedResponse;
import com.mxr.integration.Response.PersonSummary;
import com.mxr.integration.Response.ProcessedResponse;
import com.mxr.integration.exceptions.AgifyExceptions.NullAgeException;
import com.mxr.integration.exceptions.InvalidNameException;
import com.mxr.integration.exceptions.MissingGenderizeDataException;
import com.mxr.integration.exceptions.MissingOrEmptyNameException;
import com.mxr.integration.exceptions.NationalizeExceptions.MissingCountryDataException;
import com.mxr.integration.exceptions.PersonNotFoundException;
import com.mxr.integration.model.CountryData;
import com.mxr.integration.model.Person;
import com.mxr.integration.repo.PersonRepoImpl;
import com.mxr.integration.spec.PersonSpecification;
import com.mxr.integration.util.CountryNameMapper;
import com.mxr.integration.util.NaturalLanguageQueryParser;
import com.mxr.integration.util.NaturalLanguageQueryParser.QueryFilterDTO;

@Service
public class IntegrationService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    private final PersonRepoImpl repo;
    private final RestTemplate restTemplate = new RestTemplate();

    IntegrationService(PersonRepoImpl personRepoImpl) {
        this.repo = personRepoImpl;
    }

    public PersonRepoImpl getRepo() {
        return repo;
    }

    public ProcessedResponse savePerson(String name) {
        validateName(name);

        if (repo.existsByName(name)) {
            Person person = repo.findByNameIgnoreCase(name)
                    .orElseThrow(() -> new PersonNotFoundException("Person not found"));
            return ProcessedResponse.builder()
                    .status("success")
                    .data(person)
                    .build();
        }

        GenderizeResponse genderizeResponse = getGenderizeResponse(name);
        AgifyResponse agifyResponse = getAgifyResponse(name);
        NationalizeResponse nationalizeResponse = getNationalizeResponse(name);

        Person person = mapToPerson(genderizeResponse, agifyResponse, nationalizeResponse);
        repo.save(person);

        return ProcessedResponse.builder()
                .status("success")
                .data(person)
                .build();
    }

    public Person getPersonById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new PersonNotFoundException("Person not found"));
    }

    public MultipleProcessedResponse searchPeople(String gender, String countryId, String ageGroup) {
        Specification<Person> spec = Specification
                .where(PersonSpecification.hasGender(gender))
                .and(PersonSpecification.hasCountryId(countryId))
                .and(PersonSpecification.hasAgeGroup(ageGroup));

        List<PersonSummary> summaries = repo.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(person -> new PersonSummary(
                        person.getId(),
                        person.getName(),
                        person.getGender(),
                        person.getAge(),
                        person.getAgeGroup(),
                        person.getCountryId()))
                .toList();

        return MultipleProcessedResponse.builder()
                .status("success")
                .count(summaries.size())
                .data(summaries)
                .build();
    }

    public void deletePersonById(UUID id) {
        if (!repo.existsById(id)) {
            throw new PersonNotFoundException("Person not found with id: " + id);
        }
        repo.deleteById(id);
    }

    public PaginatedResponse<Person> searchProfilesByQuery(String q, int page, int limit) {
        validatePage(page);
        QueryFilterDTO filter = parseQuery(q);

        return filterProfiles(
                filter != null ? filter.getGender() : null,
                filter != null ? filter.getAgeGroup() : null,
                filter != null ? filter.getCountryId() : null,
                filter != null ? filter.getMinAge() : null,
                filter != null ? filter.getMaxAge() : null,
                filter != null ? filter.getMinGenderProbability() : null,
                filter != null ? filter.getMinCountryProbability() : null,
                "created_at",
                "desc",
                page,
                limit);
    }

    public QueryFilterDTO parseQuery(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        QueryFilterDTO parsed = NaturalLanguageQueryParser.parse(q);
        if (parsed == null) {
            throw new IllegalArgumentException("uninterpretable q");
        }
        return parsed;
    }

    public PaginatedResponse<Person> filterProfiles(
            String gender,
            String ageGroup,
            String countryId,
            Integer minAge,
            Integer maxAge,
            Double minGenderProbability,
            Double minCountryProbability,
            String sortBy,
            String order,
            int page,
            int limit) {

        validatePage(page);
        int effectiveLimit = normalizeLimit(limit);
        Sort sort = buildSort(sortBy, order);

        Specification<Person> spec = Specification
                .where(PersonSpecification.hasGender(gender))
                .and(PersonSpecification.hasCountryId(countryId))
                .and(PersonSpecification.hasAgeGroup(ageGroup))
                .and(PersonSpecification.minAge(minAge))
                .and(PersonSpecification.maxAge(maxAge))
                .and(PersonSpecification.minGenderProbability(minGenderProbability))
                .and(PersonSpecification.minCountryProbability(minCountryProbability));

        Pageable pageable = PageRequest.of(page - 1, effectiveLimit, sort);
        Page<Person> result = repo.findAll(spec, pageable);

        return PaginatedResponse.<Person>builder()
                .status("success")
                .page(page)
                .limit(effectiveLimit)
                .total(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .data(result.getContent())
                .build();
    }

    private Sort buildSort(String sortBy, String order) {
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String normalized = sortBy.trim().toLowerCase();
        if ("age".equals(normalized)) {
            return Sort.by(direction, "age");
        }
        if ("created_at".equals(normalized) || "createdat".equals(normalized)) {
            return Sort.by(direction, "createdAt");
        }
        if ("gender_probability".equals(normalized) || "genderprobability".equals(normalized)) {
            return Sort.by(direction, "genderProbability");
        }
        if ("country_probability".equals(normalized) || "countryprobability".equals(normalized)) {
            return Sort.by(direction, "countryProbability");
        }
        if ("country_id".equals(normalized) || "countryid".equals(normalized)) {
            return Sort.by(direction, "countryId");
        }
        if ("name".equals(normalized)) {
            return Sort.by(direction, "name");
        }

        throw new IllegalArgumentException("invalid sort_by");
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return limit > 50 ? 50 : limit;
    }

    public int getEffectiveLimit(Integer limit) {
        if (limit == null) return 10;
        return normalizeLimit(limit);
    }

    private void validatePage(int page) {
        if (page < 1) {
            throw new IllegalArgumentException("invalid page");
        }
    }

    private GenderizeResponse getGenderizeResponse(String name) {
        String genderizeUrl = "https://api.genderize.io/?name=" + name;
        try {
            GenderizeResponse genderizeResponse = restTemplate.getForObject(genderizeUrl, GenderizeResponse.class);
            if (genderizeResponse == null || genderizeResponse.getGender() == null) {
                return new GenderizeResponse(0, name, "unknown", 0.0);
            }
            return genderizeResponse;
        } catch (Exception e) {
            return new GenderizeResponse(0, name, "unknown", 0.0);
        }
    }

    private AgifyResponse getAgifyResponse(String name) {
        String agifyUrl = "https://api.agify.io?name=" + name;
        try {
            AgifyResponse agifyResponse = restTemplate.getForObject(agifyUrl, AgifyResponse.class);
            if (agifyResponse == null || agifyResponse.getAge() == null) {
                AgifyResponse fallback = new AgifyResponse();
                fallback.setName(name);
                fallback.setAge(30); // Default age
                return fallback;
            }
            return agifyResponse;
        } catch (Exception e) {
            AgifyResponse fallback = new AgifyResponse();
            fallback.setName(name);
            fallback.setAge(30);
            return fallback;
        }
    }

    private NationalizeResponse getNationalizeResponse(String name) {
        String nationalizeUrl = "https://api.nationalize.io?name=" + name;
        try {
            NationalizeResponse nationalizeResponse = restTemplate.getForObject(nationalizeUrl, NationalizeResponse.class);
            if (nationalizeResponse == null || nationalizeResponse.getCountries() == null || nationalizeResponse.getCountries().isEmpty()) {
                NationalizeResponse fallback = new NationalizeResponse();
                fallback.setName(name);
                CountryData cd = new CountryData();
                cd.setCountryId("US");
                cd.setProbability(0.1);
                fallback.setCountries(List.of(cd));
                return fallback;
            }
            return nationalizeResponse;
        } catch (Exception e) {
            NationalizeResponse fallback = new NationalizeResponse();
            fallback.setName(name);
            CountryData cd = new CountryData();
            cd.setCountryId("US");
            cd.setProbability(0.1);
            fallback.setCountries(List.of(cd));
            return fallback;
        }
    }

    private Person mapToPerson(GenderizeResponse genderizeResponse, AgifyResponse agifyResponse,
                               NationalizeResponse nationalizeResponse) {
        List<CountryData> countries = nationalizeResponse.getCountries();
        CountryData topCountry = countries.stream()
                .max((c1, c2) -> Double.compare(c1.getProbability(), c2.getProbability()))
                .orElseThrow(() -> new MissingCountryDataException("No country data available for the provided name"));

        return Person.builder()
                .name(genderizeResponse.getName())
                .gender(genderizeResponse.getGender())
                .genderProbability((float) genderizeResponse.getProbability())
                .age(agifyResponse.getAge())
                .ageGroup(calculateAgeGroup(agifyResponse.getAge()))
                .countryId(topCountry.getCountryId())
                .countryName(CountryNameMapper.getCountryName(topCountry.getCountryId()))
                .countryProbability((float) topCountry.getProbability())
                .build();
    }

    private String calculateAgeGroup(int age) {
        if (age >= 0 && age <= 12) return "child";
        if (age <= 19) return "teenager";
        if (age <= 59) return "adult";
        return "senior";
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new MissingOrEmptyNameException("Name cannot be empty", name);
        }
    }
}