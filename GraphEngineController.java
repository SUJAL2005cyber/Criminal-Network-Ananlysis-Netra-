package com.sih.demo.controller;

import com.sih.demo.service.GraphCollectionsEngine;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Exposes the pure Java Collections in-memory graph engine.
 * Use this to demonstrate core Java data-structure skills, and as a
 * fast/offline fallback if Neo4j isn't reachable during the demo.
 */
@RestController
@RequestMapping("/api/graph-engine")
@RequiredArgsConstructor
public class GraphEngineController {

    private final GraphCollectionsEngine engine;

    @PostMapping("/relationship")
    public ResponseEntity<String> addRelationship(@RequestBody RelationshipRequest req) {
        engine.addRelationship(req.getPersonA(), req.getPersonB(), req.getStrength(),
                req.getRelationType(), req.getEvidenceSource());
        return ResponseEntity.ok("Relationship added to in-memory graph");
    }

    @GetMapping("/path/bfs")
    public ResponseEntity<List<String>> shortestPathBfs(@RequestParam String personA,
                                                          @RequestParam String personB) {
        return ResponseEntity.ok(engine.shortestPathBFS(personA, personB));
    }

    @GetMapping("/path/strongest")
    public ResponseEntity<List<String>> strongestPath(@RequestParam String personA,
                                                        @RequestParam String personB) {
        return ResponseEntity.ok(engine.strongestConnectionPath(personA, personB));
    }

    @GetMapping("/influencers/degree")
    public ResponseEntity<List<Map.Entry<String, Integer>>> influencersByDegree(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(engine.topInfluencersByDegree(limit));
    }

    @GetMapping("/influencers/weighted")
    public ResponseEntity<List<Map.Entry<String, Double>>> influencersByWeightedScore(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(engine.topInfluencersByWeightedScore(limit));
    }

    @GetMapping("/associates/{name}")
    public ResponseEntity<List<GraphCollectionsEngine.Edge>> associates(@PathVariable String name) {
        return ResponseEntity.ok(engine.getDirectAssociates(name));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Integer>> stats() {
        return ResponseEntity.ok(Map.of("totalPersons", engine.totalPersons()));
    }

    @Data
    public static class RelationshipRequest {
        private String personA;
        private String personB;
        private double strength;
        private String relationType;
        private String evidenceSource;
    }
}
