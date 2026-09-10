package com.sih.demo.repository.neo4j;

import com.sih.demo.entity.neo4j.PersonNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonNodeRepository extends Neo4jRepository<PersonNode, Long> {

    Optional<PersonNode> findByName(String name);

    @Query("""
        MATCH (p:Person {name: $name})
        OPTIONAL MATCH (p)-[:ASSOCIATE_OF*1..2]-(connected:Person)
        RETURN DISTINCT connected
        """)
    List<PersonNode> findNetworkAround(@Param("name") String name);

    @Query("""
        MATCH (p:Person)-[r]-(other)
        RETURN p
        ORDER BY size([(p)-->(x) | x]) DESC
        LIMIT $limit
        """)
    List<PersonNode> findTopInfluencersByDegree(@Param("limit") int limit);

    @Query("""
        MATCH (a:Person {name: $nameA}),
              (b:Person {name: $nameB}),
              path = shortestPath((a)-[:ASSOCIATE_OF*..6]-(b))
        RETURN nodes(path)
        """)
    List<PersonNode> findPersonsOnShortestPath(
            @Param("nameA") String nameA,
            @Param("nameB") String nameB);
}
