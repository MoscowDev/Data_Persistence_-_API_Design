package com.mxr.integration.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import com.mxr.integration.model.Person;

@Repository
public interface PersonRepoImpl extends JpaRepository<Person, UUID>, JpaSpecificationExecutor<Person> {
    Optional<Person> findByNameIgnoreCase(String name);

    void deleteByName(String name);

    Person findByName(String name);

    Optional<Person> findById(UUID id);

    boolean existsByName(String name);
}
