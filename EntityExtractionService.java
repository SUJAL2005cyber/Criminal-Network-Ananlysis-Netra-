package com.sih.demo.service;

import com.sih.demo.dto.ExtractedEntitiesDto;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EntityExtractionService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+91[-\\s]?)?[6-9]\\d{9}\\b");
    private static final Pattern VEHICLE_PATTERN = Pattern.compile("\\b[A-Z]{2}[- ]?\\d{1,2}[- ]?[A-Z]{1,2}[- ]?\\d{4}\\b");
    private static final Pattern LOCATION_PATTERN = Pattern.compile("(?:at|near|in)\\s+([A-Z][a-zA-Z]+(?:\\s[A-Z][a-zA-Z]+){0,3})");
    private static final Pattern NAME_PATTERN = Pattern.compile("(?:Mr\\.?|Mrs\\.?|Ms\\.?|accused|suspect|witness|complainant)\\s+([A-Z][a-z]+(?:\\s[A-Z][a-z]+){0,2})");

    public ExtractedEntitiesDto extract(String rawText) {
        Set<String> persons = new LinkedHashSet<>();
        Set<String> phones = new LinkedHashSet<>();
        Set<String> locations = new LinkedHashSet<>();
        Set<String> vehicles = new LinkedHashSet<>();
        if (rawText == null || rawText.isBlank()) {
            return new ExtractedEntitiesDto(persons, phones, locations, vehicles);
        }
        find(NAME_PATTERN, rawText, persons);
        find(PHONE_PATTERN, rawText, phones);
        find(LOCATION_PATTERN, rawText, locations);
        find(VEHICLE_PATTERN, rawText, vehicles);
        return new ExtractedEntitiesDto(persons, phones, locations, vehicles);
    }

    private void find(Pattern pattern, String text, Set<String> output) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String value = matcher.groupCount() > 0 ? matcher.group(1) : matcher.group();
            if (value != null && !value.isBlank()) output.add(value.trim());
        }
    }
}
