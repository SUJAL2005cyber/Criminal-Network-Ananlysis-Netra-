package com.sih.demo.repository.neo4j;

import com.sih.demo.entity.neo4j.PhoneNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import java.util.Optional;

public interface PhoneNodeRepository extends Neo4jRepository<PhoneNode, Long> {
    Optional<PhoneNode> findByNumber(String number);
}
