package com.mxr.integration.util;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaturalLanguageQueryParser {

    private static final Map<String, String> COUNTRY_MAPPING = new LinkedHashMap<>();

    static {
        COUNTRY_MAPPING.put("south africa", "ZA");
        COUNTRY_MAPPING.put("democratic republic of congo", "CD");
        COUNTRY_MAPPING.put("central african republic", "CF");
        COUNTRY_MAPPING.put("equatorial guinea", "GQ");
        COUNTRY_MAPPING.put("sierra leone", "SL");
        COUNTRY_MAPPING.put("south sudan", "SS");
        COUNTRY_MAPPING.put("guinea-bissau", "GW");
        COUNTRY_MAPPING.put("ivory coast", "CI");
        COUNTRY_MAPPING.put("cape verde", "CV");
        COUNTRY_MAPPING.put("sao tome", "ST");
        COUNTRY_MAPPING.put("burkina faso", "BF");
        COUNTRY_MAPPING.put("mauritius", "MU");
        COUNTRY_MAPPING.put("seychelles", "SC");
        COUNTRY_MAPPING.put("botswana", "BW");
        COUNTRY_MAPPING.put("namibia", "NA");
        COUNTRY_MAPPING.put("lesotho", "LS");
        COUNTRY_MAPPING.put("zimbabwe", "ZW");
        COUNTRY_MAPPING.put("zambia", "ZM");
        COUNTRY_MAPPING.put("malawi", "MW");
        COUNTRY_MAPPING.put("tanzania", "TZ");
        COUNTRY_MAPPING.put("uganda", "UG");
        COUNTRY_MAPPING.put("nigeria", "NG");
        COUNTRY_MAPPING.put("ghana", "GH");
        COUNTRY_MAPPING.put("kenya", "KE");
        COUNTRY_MAPPING.put("ethiopia", "ET");
        COUNTRY_MAPPING.put("cameroon", "CM");
        COUNTRY_MAPPING.put("senegal", "SN");
        COUNTRY_MAPPING.put("morocco", "MA");
        COUNTRY_MAPPING.put("algeria", "DZ");
        COUNTRY_MAPPING.put("tunisia", "TN");
        COUNTRY_MAPPING.put("rwanda", "RW");
        COUNTRY_MAPPING.put("sudan", "SD");
        COUNTRY_MAPPING.put("djibouti", "DJ");
        COUNTRY_MAPPING.put("eritrea", "ER");
        COUNTRY_MAPPING.put("somalia", "SO");
        COUNTRY_MAPPING.put("liberia", "LR");
        COUNTRY_MAPPING.put("guinea", "GN");
        COUNTRY_MAPPING.put("mali", "ML");
        COUNTRY_MAPPING.put("mauritania", "MR");
        COUNTRY_MAPPING.put("gabon", "GA");
        COUNTRY_MAPPING.put("congo", "CG");
        COUNTRY_MAPPING.put("chad", "TD");
        COUNTRY_MAPPING.put("niger", "NE");
        COUNTRY_MAPPING.put("togo", "TG");
        COUNTRY_MAPPING.put("benin", "BJ");
        COUNTRY_MAPPING.put("comoros", "KM");
        COUNTRY_MAPPING.put("maldives", "MV");
    }

    public static QueryFilterDTO parse(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        String lowerQuery = query.toLowerCase(Locale.ROOT).trim();
        QueryFilterDTO filter = new QueryFilterDTO();
        boolean matched = false;

        if (containsWholeWord(lowerQuery, "female") || containsWholeWord(lowerQuery, "women") || containsWholeWord(lowerQuery, "girls") || containsWholeWord(lowerQuery, "woman") || containsWholeWord(lowerQuery, "girl")) {
            filter.setGender("female");
            matched = true;
        } else if (containsWholeWord(lowerQuery, "male") || containsWholeWord(lowerQuery, "men") || containsWholeWord(lowerQuery, "boys") || containsWholeWord(lowerQuery, "man") || containsWholeWord(lowerQuery, "boy")) {
            filter.setGender("male");
            matched = true;
        }

        if (containsWholeWord(lowerQuery, "teenager") || containsWholeWord(lowerQuery, "teen")) {
            filter.setAgeGroup("teenager");
            filter.setMinAge(13);
            filter.setMaxAge(19);
            matched = true;
        } else if (containsWholeWord(lowerQuery, "adult")) {
            filter.setAgeGroup("adult");
            filter.setMinAge(20);
            filter.setMaxAge(59);
            matched = true;
        } else if (containsWholeWord(lowerQuery, "child") || containsWholeWord(lowerQuery, "kid")) {
            filter.setAgeGroup("child");
            filter.setMinAge(0);
            filter.setMaxAge(12);
            matched = true;
        } else if (containsWholeWord(lowerQuery, "senior") || containsWholeWord(lowerQuery, "elderly")) {
            filter.setAgeGroup("senior");
            filter.setMinAge(60);
            matched = true;
        }

        Integer minAge = extractAgeBoundary(lowerQuery, "above", "over");
        if (minAge != null) {
            filter.setMinAge(minAge);
            matched = true;
        }

        Integer maxAge = extractAgeBoundary(lowerQuery, "below", "under");
        if (maxAge != null) {
            filter.setMaxAge(maxAge);
            matched = true;
        }

        if (containsWholeWord(lowerQuery, "probability") || containsWholeWord(lowerQuery, "confidence")) {
            Double prob = extractProbability(lowerQuery);
            if (prob != null) {
                if (lowerQuery.contains("gender") || lowerQuery.contains("sex")) {
                    filter.setMinGenderProbability(prob);
                } else if (lowerQuery.contains("country") || lowerQuery.contains("nation")) {
                    filter.setMinCountryProbability(prob);
                } else {
                    // Default to gender probability if ambiguous? 
                    // Or set both?
                    filter.setMinGenderProbability(prob);
                }
                matched = true;
            }
        }

        String countryId = extractCountry(lowerQuery);
        if (countryId != null) {
            filter.setCountryId(countryId);
            matched = true;
        }

        return matched ? filter : null;
    }

    private static boolean containsWholeWord(String text, String word) {
        return text.matches(".*\\b" + Pattern.quote(word) + "\\b.*");
    }

    private static Integer extractAgeBoundary(String query, String keyword1, String keyword2) {
        Integer value = extractNumberAfterKeyword(query, keyword1);
        if (value != null) {
            return value;
        }
        return extractNumberAfterKeyword(query, keyword2);
    }

    private static Integer extractNumberAfterKeyword(String query, String keyword) {
        Matcher matcher = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\s+(\\d{1,3})\\b").matcher(query);
        if (matcher.find()) {
            return Integer.valueOf(matcher.group(1));
        }
        return null;
    }

    private static Double extractProbability(String query) {
        Matcher matcher = Pattern.compile("(\\d+(\\.\\d+)?)\\b").matcher(query);
        while (matcher.find()) {
            double val = Double.parseDouble(matcher.group(1));
            if (val >= 0 && val <= 1) {
                return val;
            }
        }
        return null;
    }

    private static String extractCountry(String query) {
        return COUNTRY_MAPPING.entrySet().stream()
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