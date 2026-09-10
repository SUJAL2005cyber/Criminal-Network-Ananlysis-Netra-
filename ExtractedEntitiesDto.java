package com.sih.demo.dto;

import lombok.*;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedEntitiesDto {
    private Set<String> persons;
    private Set<String> phoneNumbers;
    private Set<String> locations;
    private Set<String> vehicleNumbers;
}
