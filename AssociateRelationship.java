package com.sih.demo.entity.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

/**
 * Rich relationship (edge with properties) between two persons.
 * Storing evidence + strength lets the frontend show WHY two suspects
 * are connected, not just THAT they are connected.
 */
@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssociateRelationship {

    @Id
    @GeneratedValue
    private Long id;

    @TargetNode
    private PersonNode target;

    private String relationType;   // e.g. "FAMILY", "FINANCIAL", "CALL_RECORD", "CO_ACCUSED"
    private String evidenceSource; // e.g. "CDR-2026-0031", "FIR-118/2026"
    private Double strength;       // 0.0 - 1.0, frequency/weight of interaction
    private String firstContact;
    private String lastContact;
}
