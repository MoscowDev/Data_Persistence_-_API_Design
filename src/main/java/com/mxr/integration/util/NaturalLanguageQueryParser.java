package com.mxr.integration.util;

import java.util.HashMap;
import java.util.Map;

public class NaturalLanguageQueryParser {

    private static final Map<String, String> COUNTRY_MAPPING = new HashMap<>();
    private static final Map<String, Integer[]> AGE_RANGE_MAPPING = new HashMap<>();

    static {
        // Common country names to country IDs
        COUNTRY_MAPPING.put("nigeria", "NG");
        COUNTRY_MAPPING.put("benin", "BJ");
        COUNTRY_MAPPING.put("ghana", "GH");
        COUNTRY_MAPPING.put("kenya", "KE");
        COUNTRY_MAPPING.put("uganda", "UG");
        COUNTRY_MAPPING.put("tanzania", "TZ");
        COUNTRY_MAPPING.put("south africa", "ZA");
        COUNTRY_MAPPING.put("egypt", "EG");
        COUNTRY_MAPPING.put("ethiopia", "ET");
        COUNTRY_MAPPING.put("cameroon", "CM");
        COUNTRY_MAPPING.put("ivory coast", "CI");
        COUNTRY_MAPPING.put("senegal", "SN");
        COUNTRY_MAPPING.put("morocco", "MA");
        COUNTRY_MAPPING.put("algeria", "DZ");
        COUNTRY_MAPPING.put("tunisia", "TN");
        COUNTRY_MAPPING.put("rwanda", "RW");
        COUNTRY_MAPPING.put("malawi", "MW");
        COUNTRY_MAPPING.put("zambia", "ZM");
        COUNTRY_MAPPING.put("zimbabwe", "ZW");
        COUNTRY_MAPPING.put("botswana", "BW");
        COUNTRY_MAPPING.put("namibia", "NA");
        COUNTRY_MAPPING.put("lesotho", "LS");
        COUNTRY_MAPPING.put("mauritius", "MU");
        COUNTRY_MAPPING.put("seychelles", "SC");
        COUNTRY_MAPPING.put("malawi", "MW");
        COUNTRY_MAPPING.put("sudan", "SD");
        COUNTRY_MAPPING.put("south sudan", "SS");
        COUNTRY_MAPPING.put("djibouti", "DJ");
        COUNTRY_MAPPING.put("eritrea", "ER");
        COUNTRY_MAPPING.put("somalia", "SO");
        COUNTRY_MAPPING.put("liberia", "LR");
        COUNTRY_MAPPING.put("sierra leone", "SL");
        COUNTRY_MAPPING.put("guinea", "GN");
        COUNTRY_MAPPING.put("guinea-bissau", "GW");
        COUNTRY_MAPPING.put("mali", "ML");
        COUNTRY_MAPPING.put("mauritania", "MR");
        COUNTRY_MAPPING.put("cape verde", "CV");
        COUNTRY_MAPPING.put("sao tome", "ST");
        COUNTRY_MAPPING.put("gabon", "GA");
        COUNTRY_MAPPING.put("congo", "CG");
        COUNTRY_MAPPING.put("democratic republic of congo", "CD");
        COUNTRY_MAPPING.put("drc", "CD");
        COUNTRY_MAPPING.put("equatorial guinea", "GQ");
        COUNTRY_MAPPING.put("central african republic", "CF");
        COUNTRY_MAPPING.put("chad", "TD");
        COUNTRY_MAPPING.put("niger", "NE");
        COUNTRY_MAPPING.put("burkina faso", "BF");
        COUNTRY_MAPPING.put("togo", "TG");
        COUNTRY_MAPPING.put("benin", "BJ");
        COUNTRY_MAPPING.put("comoros", "KM");
        COUNTRY_MAPPING.put("maldives", "MV");
        COUNTRY_MAPPING.put("mauritius", "MU");

        // Age ranges
        AGE_RANGE_MAPPING.put("young", new Integer[]{16, 24});
        AGE_RANGE_MAPPING.put("child", new Integer[]{0, 12});
        AGE_RANGE_MAPPING.put("teenager", new Integer[]{13, 19});
        AGE_RANGE_MAPPING.put("adult", new Integer[]{20, 59});
        AGE_RANGE_MAPPING.put("senior", new Integer[]{60, 120});
    }

    public static QueryFilterDTO parse(String query) {
        if (query == null || query.trim().isEmpty()) {
            return null;
        }

        QueryFilterDTO filter = new QueryFilterDTO();
        String lowerQuery = query.toLowerCase().trim();

        // Parse gender
        if (lowerQuery.contains("male") && !lowerQuery.contains("female")) {
            filter.setGender("male");
        } else if (lowerQuery.contains("female")) {
            filter.setGender("female");
        }

        // Parse age groups
        if (lowerQuery.contains("teenager")) {
            filter.setAgeGroup("teenager");
            if (!lowerQuery.contains("above") && !lowerQuery.contains("over")) {
                filter.setMinAge(13);
                filter.setMaxAge(19);
            }
        } else if (lowerQuery.contains("adult")) {
            filter.setAgeGroup("adult");
        } else if (lowerQuery.contains("child")) {
            filter.setAgeGroup("child");
        } else if (lowerQuery.contains("senior")) {
            filter.setAgeGroup("senior");
        }

        // Parse "young" keyword (maps to ages 16-24)
        if (lowerQuery.contains("young")) {
            filter.setMinAge(16);
            filter.setMaxAge(24);
        }

        // Parse age numbers
        parseAgeNumbers(lowerQuery, filter);

        // Parse country
        String countryId = extractCountry(lowerQuery);
        if (countryId != null) {
            filter.setCountryId(countryId);
        }

        return filter;
    }

    private static void parseAgeNumbers(String query, QueryFilterDTO filter) {
        // Look for patterns like "above 30", "over 25", "17 and up"
        if (query.matches(".*above\\s+\\d+.*") || query.matches(".*over\\s+\\d+.*")) {
            int age = extractNumber(query);
            if (age > 0) {
                filter.setMinAge(age);
            }
        }

        if (query.matches(".*below\\s+\\d+.*") || query.matches(".*under\\s+\\d+.*")) {
            int age = extractNumber(query);
            if (age > 0) {
                filter.setMaxAge(age);
            }
        }
    }

    private static int extractNumber(String query) {
        String[] words = query.split("\\s+");
        for (int i = 0; i < words.length - 1; i++) {
            if (words[i].matches("above|over|below|under")) {
                try {
                    return Integer.parseInt(words[i + 1]);
                } catch (NumberFormatException e) {
                    // continue
                }
            }
        }
        return -1;
    }

    private static String extractCountry(String query) {
        // Check for country names in descending order of length to match "South Africa" before "Africa"
        return COUNTRY_MAPPING.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()))
                .filter(entry -> query.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public static class QueryFilterDTO {
        private String gender;
        private String ageGroup;
        private String countryId;
        private Integer minAge;
        private Integer maxAge;
        private Double minGenderProbability;
        private Double minCountryProbability;

        // Getters and Setters
        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getAgeGroup() {
            return ageGroup;
        }

        public void setAgeGroup(String ageGroup) {
            this.ageGroup = ageGroup;
        }

        public String getCountryId() {
            return countryId;
        }

        public void setCountryId(String countryId) {
            this.countryId = countryId;
        }

        public Integer getMinAge() {
            return minAge;
        }

        public void setMinAge(Integer minAge) {
            this.minAge = minAge;
        }

        public Integer getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(Integer maxAge) {
            this.maxAge = maxAge;
        }

        public Double getMinGenderProbability() {
            return minGenderProbability;
        }

        public void setMinGenderProbability(Double minGenderProbability) {
            this.minGenderProbability = minGenderProbability;
        }

        public Double getMinCountryProbability() {
            return minCountryProbability;
        }

        public void setMinCountryProbability(Double minCountryProbability) {
            this.minCountryProbability = minCountryProbability;
        }
    }
}

