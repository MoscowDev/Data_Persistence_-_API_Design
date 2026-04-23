package com.mxr.integration.spec;

import org.springframework.data.jpa.domain.Specification;
import com.mxr.integration.model.Person;

public class PersonSpecification {

    public static Specification<Person> hasGender(String gender) {
        return (root, query, cb) -> gender == null ? null
                : cb.equal(cb.lower(root.get("gender")), gender.toLowerCase());
    }

    public static Specification<Person> hasCountryId(String countryId) {
        return (root, query, cb) -> countryId == null ? null
                : cb.equal(cb.lower(root.get("countryId")), countryId.toLowerCase());
    }

    public static Specification<Person> hasAgeGroup(String ageGroup) {
        return (root, query, cb) -> ageGroup == null ? null
                : cb.equal(cb.lower(root.get("ageGroup")), ageGroup.toLowerCase());
    }

    public static Specification<Person> hasName(String name) {
        return (root, query, cb) -> name == null ? null : cb.equal(cb.lower(root.get("name")), name.toLowerCase());
    }

    public static Specification<Person> minAge(Integer minAge) {
        return (root, query, cb) -> minAge == null ? null
                : cb.greaterThanOrEqualTo(root.get("age"), minAge);
    }

    public static Specification<Person> maxAge(Integer maxAge) {
        return (root, query, cb) -> maxAge == null ? null
                : cb.lessThanOrEqualTo(root.get("age"), maxAge);
    }

    public static Specification<Person> minGenderProbability(Double minProb) {
        return (root, query, cb) -> minProb == null ? null
                : cb.greaterThanOrEqualTo(root.get("genderProbability"), minProb);
    }

    public static Specification<Person> minCountryProbability(Double minProb) {
        return (root, query, cb) -> minProb == null ? null
                : cb.greaterThanOrEqualTo(root.get("countryProbability"), minProb);
    }
}