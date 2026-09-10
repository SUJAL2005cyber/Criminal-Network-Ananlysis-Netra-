package com.sih.demo.entity.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

@Node("Location")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationNode {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private Double latitude;
    private Double longitude;
    private String locationType; // RESIDENCE / MEETING_POINT / CRIME_SCENE
}
