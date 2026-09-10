package com.sih.demo.entity.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

@Node("Phone")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneNode {

    @Id
    @GeneratedValue
    private Long id;

    private String number;
    private String carrier;
    private String status; // ACTIVE / BURNER / UNKNOWN
}
