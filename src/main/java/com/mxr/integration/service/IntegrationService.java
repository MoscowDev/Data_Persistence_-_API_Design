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
import com.mxr.integration.Response.PersonExistsResponse;
import com.mxr.integration.Response.PersonSummary;
import com.mxr.integration.Response.PaginatedResponse;
import com.mxr.integration.Response.ProcessedResponse;
import com.mxr.integration.exceptions.MissingGenderizeDataException;
import com.mxr.integration.exceptions.MissingOrEmptyNameException;
import com.mxr.integration.exceptions.PersonNotFoundException;
import com.mxr.integration.exceptions.AgifyExceptions.NullAgeException;
import com.mxr.integration.exceptions.NationalizeExceptions.MissingCountryDataException;
import com.mxr.integration.exceptions.InvalidNameException;
import com.mxr.integration.model.CountryData;
import com.mxr.integration.model.Person;
import com.mxr.integration.repo.PersonRepoImpl;
import com.mxr.integration.spec.PersonSpecification;
import com.mxr.integration.util.CountryNameMapper;

@Service
public class IntegrationService {

    private final PersonRepoImpl repo;

    IntegrationService(PersonRepoImpl personRepoImpl) {
        this.repo = personRepoImpl;
    }

    RestTemplate restTemplate = new RestTemplate();

    public ProcessedResponse savePerson(String name) {
        validateName(name);
        if (repo.existsByName(name)) {
            Person person = repo.findByNameIgnoreCase(name).get();
            return new PersonExistsResponse("success", person, "Profile already exists");
        }
        GenderizeResponse genderizeResponse = getGenderizeResponse(name);
        AgifyResponse agifyResponse = getAgifyResponse(name);
        NationalizeResponse nationalizeResponse = getNationalizeResponse(name);
        Person person = mapToPerson(genderizeResponse, agifyResponse, nationalizeResponse);
        repo.save(person);

        return new ProcessedResponse("success", person);
    }

    public List<PersonSummary> searchPeople(String gender, String countryId, String ageGroup) {
        Specification<Person> spec = Specification
                .where(PersonSpecification.hasGender(gender))
                .and(PersonSpecification.hasCountryId(countryId))
                .and(PersonSpecification.hasAgeGroup(ageGroup));

        return mapToPersonSummary(repo.findAll(spec));
    }

    private List<PersonSummary> mapToPersonSummary(List<Person> all) {
        return all.stream().map(person -> new PersonSummary(person.getId(), person.getName(), person.getGender(), person.getAge(), calculateAgeGroup(person.getAge()), person.getCountryId())).toList();
    }

    public Person getPersonById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new PersonNotFoundException("Person not found"));
    }

    public void deletePerson(String name) {
        repo.deleteByName(name);
    }

    public void deletePersonById(UUID id) {
        if (!repo.existsById(id)) {
            throw new PersonNotFoundException("Person not found with id: " + id);
        }
        repo.deleteById(id);
    }

    public GenderizeResponse getGenderizeResponse(String name) {

        String genderizeUrl = "https://api.genderize.io/?name=" + name;
        GenderizeResponse genderizeResponse = restTemplate.getForObject(genderizeUrl, GenderizeResponse.class);

        if (genderizeResponse == null)
            throw new MissingGenderizeDataException("Genderize returned an invalid response");

        String gender = genderizeResponse.getGender();
        int count = genderizeResponse.getSampleSize();

        if (gender == null || count == 0)
            throw new MissingGenderizeDataException("Genderize returned an invalid response");
        return genderizeResponse;
    }

    public AgifyResponse getAgifyResponse(String name) {
        String agifyUrl = "https://api.agify.io?name=" + name;
        AgifyResponse agifyResponse = restTemplate.getForObject(agifyUrl, AgifyResponse.class);

        if (agifyResponse.getAge() == null)
            throw new NullAgeException("Agify returned an invalid response");
        
        return agifyResponse;
    }

    public NationalizeResponse getNationalizeResponse(String name) {
        String nationalizeUrl = "https://api.nationalize.io?name=" + name;
        NationalizeResponse nationalizeResponse = restTemplate.getForObject(nationalizeUrl, NationalizeResponse.class);
        List<CountryData> countries = nationalizeResponse.getCountries();
        if (countries == null || countries.isEmpty())
            throw new MissingCountryDataException("Nationalize returned an invalid response");

        return nationalizeResponse;
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

        // Build specification
        Specification<Person> spec = Specification
                .where(PersonSpecification.hasGender(gender))
                .and(PersonSpecification.hasCountryId(countryId))
                .and(PersonSpecification.hasAgeGroup(ageGroup))
                .and(PersonSpecification.minAge(minAge))
                .and(PersonSpecification.maxAge(maxAge))
                .and(PersonSpecification.minGenderProbability(minGenderProbability))
                .and(PersonSpecification.minCountryProbability(minCountryProbability));

        // Build sort
        Sort sort = buildSort(sortBy, order);

        // Create pageable
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        // Execute query
        Page<Person> result = repo.findAll(spec, pageable);

        return PaginatedResponse.<Person>builder()
                .status("success")
                .page(page)
                .limit(limit)
                .total(result.getTotalElements())
                .data(result.getContent())
                .build();
    }

    private Sort buildSort(String sortBy, String order) {
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;

        if ("age".equalsIgnoreCase(sortBy)) {
            return Sort.by(direction, "age");
        } else if ("created_at".equalsIgnoreCase(sortBy) || "createdat".equalsIgnoreCase(sortBy)) {
            return Sort.by(direction, "createdAt");
        } else if ("gender_probability".equalsIgnoreCase(sortBy) || "genderprobability".equalsIgnoreCase(sortBy)) {
            return Sort.by(direction, "genderProbability");
        }

        return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    public Person mapToPerson(GenderizeResponse genderizeResponse, AgifyResponse agifyResponse,
            NationalizeResponse nationalizeResponse) {
        List<CountryData> countries = nationalizeResponse.getCountries();
        CountryData topCountry = getCountryWithHighestProbability(countries);

        return Person.builder()
                .name(genderizeResponse.getName())
                .gender(genderizeResponse.getGender())
                .genderProbability(genderizeResponse.getProbability())
                .sampleSize(genderizeResponse.getSampleSize())
                .age(agifyResponse.getAge())
                .ageGroup(calculateAgeGroup(agifyResponse.getAge()))
                .countryId(topCountry.getCountryId())
                .countryName(CountryNameMapper.getCountryName(topCountry.getCountryId()))
                .countryProbability(topCountry.getProbability())
                .build();
    }

    private String calculateAgeGroup(int age) {
        if (age <= 12 && age >= 0)
            return "child";
        if (age <= 19 && age >= 13)
            return "teenager";
        if (age <= 59 && age >= 20)
            return "adult";
        if (age >= 60)
            return "senior";
        return "senior";
    }

    private CountryData getCountryWithHighestProbability(List<CountryData> countries) {
        return countries.stream()
                .max((c1, c2) -> Double.compare(c1.getProbability(), c2.getProbability()))
                .orElseThrow(() -> new MissingCountryDataException("No country data available for the provided name"));
    }

    private void validateName(String name) {
        if (name == null || name.isBlank())
            throw new MissingOrEmptyNameException("Name cannot be empty", name);
        if (name.matches(".*\\d.*"))
            throw new InvalidNameException("Name must contain only letters");
    }

}
