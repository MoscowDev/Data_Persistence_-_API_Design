package com.mxr.integration.util;

import java.util.HashMap;
import java.util.Map;

public class CountryNameMapper {
    private static final Map<String, String> COUNTRY_MAP = new HashMap<>();

    static {
        COUNTRY_MAP.put("NG", "Nigeria");
        COUNTRY_MAP.put("BJ", "Benin");
        COUNTRY_MAP.put("GH", "Ghana");
        COUNTRY_MAP.put("KE", "Kenya");
        COUNTRY_MAP.put("UG", "Uganda");
        COUNTRY_MAP.put("TZ", "Tanzania");
        COUNTRY_MAP.put("ZA", "South Africa");
        COUNTRY_MAP.put("EG", "Egypt");
        COUNTRY_MAP.put("ET", "Ethiopia");
        COUNTRY_MAP.put("CM", "Cameroon");
        COUNTRY_MAP.put("CI", "Ivory Coast");
        COUNTRY_MAP.put("SN", "Senegal");
        COUNTRY_MAP.put("MA", "Morocco");
        COUNTRY_MAP.put("DZ", "Algeria");
        COUNTRY_MAP.put("TN", "Tunisia");
        COUNTRY_MAP.put("RW", "Rwanda");
        COUNTRY_MAP.put("MW", "Malawi");
        COUNTRY_MAP.put("ZM", "Zambia");
        COUNTRY_MAP.put("ZW", "Zimbabwe");
        COUNTRY_MAP.put("BW", "Botswana");
        COUNTRY_MAP.put("NA", "Namibia");
        COUNTRY_MAP.put("LS", "Lesotho");
        COUNTRY_MAP.put("MU", "Mauritius");
        COUNTRY_MAP.put("SC", "Seychelles");
        COUNTRY_MAP.put("SD", "Sudan");
        COUNTRY_MAP.put("SS", "South Sudan");
        COUNTRY_MAP.put("DJ", "Djibouti");
        COUNTRY_MAP.put("ER", "Eritrea");
        COUNTRY_MAP.put("SO", "Somalia");
        COUNTRY_MAP.put("LR", "Liberia");
        COUNTRY_MAP.put("SL", "Sierra Leone");
        COUNTRY_MAP.put("GN", "Guinea");
        COUNTRY_MAP.put("GW", "Guinea-Bissau");
        COUNTRY_MAP.put("ML", "Mali");
        COUNTRY_MAP.put("MR", "Mauritania");
        COUNTRY_MAP.put("CV", "Cape Verde");
        COUNTRY_MAP.put("ST", "Sao Tome");
        COUNTRY_MAP.put("GA", "Gabon");
        COUNTRY_MAP.put("CG", "Congo");
        COUNTRY_MAP.put("CD", "Democratic Republic of Congo");
        COUNTRY_MAP.put("GQ", "Equatorial Guinea");
        COUNTRY_MAP.put("CF", "Central African Republic");
        COUNTRY_MAP.put("TD", "Chad");
        COUNTRY_MAP.put("NE", "Niger");
        COUNTRY_MAP.put("BF", "Burkina Faso");
        COUNTRY_MAP.put("TG", "Togo");
        COUNTRY_MAP.put("KM", "Comoros");
        COUNTRY_MAP.put("MV", "Maldives");

        COUNTRY_MAP.put("US", "United States");
        COUNTRY_MAP.put("GB", "United Kingdom");
        COUNTRY_MAP.put("CA", "Canada");
        COUNTRY_MAP.put("AU", "Australia");
        COUNTRY_MAP.put("DE", "Germany");
        COUNTRY_MAP.put("FR", "France");
        COUNTRY_MAP.put("IT", "Italy");
        COUNTRY_MAP.put("ES", "Spain");
        COUNTRY_MAP.put("NL", "Netherlands");
        COUNTRY_MAP.put("BE", "Belgium");
        COUNTRY_MAP.put("CH", "Switzerland");
        COUNTRY_MAP.put("SE", "Sweden");
        COUNTRY_MAP.put("NO", "Norway");
        COUNTRY_MAP.put("DK", "Denmark");
        COUNTRY_MAP.put("FI", "Finland");
        COUNTRY_MAP.put("PL", "Poland");
        COUNTRY_MAP.put("CZ", "Czech Republic");
        COUNTRY_MAP.put("SK", "Slovakia");
        COUNTRY_MAP.put("HU", "Hungary");
        COUNTRY_MAP.put("RO", "Romania");
        COUNTRY_MAP.put("BG", "Bulgaria");
        COUNTRY_MAP.put("GR", "Greece");
        COUNTRY_MAP.put("PT", "Portugal");
        COUNTRY_MAP.put("IE", "Ireland");
        COUNTRY_MAP.put("JP", "Japan");
        COUNTRY_MAP.put("KR", "South Korea");
        COUNTRY_MAP.put("CN", "China");
        COUNTRY_MAP.put("IN", "India");
        COUNTRY_MAP.put("BR", "Brazil");
        COUNTRY_MAP.put("MX", "Mexico");
        COUNTRY_MAP.put("AR", "Argentina");
        COUNTRY_MAP.put("RU", "Russia");
        COUNTRY_MAP.put("UA", "Ukraine");
        COUNTRY_MAP.put("TR", "Turkey");
        COUNTRY_MAP.put("SA", "Saudi Arabia");
        COUNTRY_MAP.put("AE", "United Arab Emirates");
        COUNTRY_MAP.put("IL", "Israel");
        COUNTRY_MAP.put("SG", "Singapore");
        COUNTRY_MAP.put("MY", "Malaysia");
        COUNTRY_MAP.put("TH", "Thailand");
        COUNTRY_MAP.put("VN", "Vietnam");
        COUNTRY_MAP.put("PH", "Philippines");
        COUNTRY_MAP.put("ID", "Indonesia");
        COUNTRY_MAP.put("NZ", "New Zealand");
    }

    public static String getCountryName(String countryId) {
        if (countryId == null) {
            return null;
        }
        return COUNTRY_MAP.getOrDefault(countryId.toUpperCase(), countryId);
    }
}

