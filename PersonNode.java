package com.sih.demo.entity.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a suspect / individual in the criminal network graph.
 * Relationships to other people, phones, locations and organizations
 * are what let us uncover "hidden" connections investigators can't
 * spot manually.
 */
@Node("Person")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonNode {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String aliasNames;       // comma separated known aliases
    private String criminalRecordId; // FK reference to MySQL CrimeCase table
    private String riskLevel;        // LOW / MEDIUM / HIGH
    private Double influenceScore;   // computed via centrality

    @Relationship(type = "ASSOCIATE_OF", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<AssociateRelationship> associates = new HashSet<>();

    @Relationship(type = "USES_PHONE", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<PhoneNode> phoneNumbers = new HashSet<>();

    @Relationship(type = "LOCATED_AT", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<LocationNode> locations = new HashSet<>();

    @Relationship(type = "LINKED_TO", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<OrganizationNode> organizations = new HashSet<>();
}
