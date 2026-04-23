package com.mxr.integration.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.UUID;
import com.fasterxml.uuid.Generators;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    @Id
    private UUID id;

    @NotNull
    @Column(unique = true)
    private String name;

    @NotNull
    private String gender;

    @JsonProperty("gender_probability")
    private float genderProbability;

    private int age;

    @JsonProperty("age_group")
    private String ageGroup;

    @JsonProperty("country_id")
    private String countryId;

    @JsonProperty("country_name")
    private String countryName;

    @JsonProperty("country_probability")
    private float countryProbability;

    @JsonProperty("created_at")
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = Generators.timeBasedEpochGenerator().generate();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
