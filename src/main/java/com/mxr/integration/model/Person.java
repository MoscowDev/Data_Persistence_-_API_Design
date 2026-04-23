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

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = Generators.timeBasedEpochGenerator().generate();
        }
    }
    
    @NotNull
    @Column(unique = true)
    private String name;

    @NotNull
    private String gender;

    @JsonProperty("genderProbability")
    private double genderProbability;

    @JsonProperty("sampleSize")
    private int sampleSize;

    private int age;

    @JsonProperty("ageGroup")
    private String ageGroup;

    @JsonProperty("countryId")
    private String countryId;

    @JsonProperty("countryName")
    private String countryName;

    @JsonProperty("countryProbability")
    private double countryProbability;

    @JsonProperty("createdAt")
    @CreationTimestamp
    private Instant createdAt;
}
