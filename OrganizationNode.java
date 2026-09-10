package com.sih.demo.entity.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

@Node("Organization")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationNode {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String orgType; // GANG / SHELL_COMPANY / SYNDICATE
}
