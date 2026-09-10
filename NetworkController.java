package com.sih.demo.controller;

import com.sih.demo.dto.ExtractedEntitiesDto;
import com.sih.demo.entity.neo4j.PersonNode;
import com.sih.demo.service.NetworkAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/network")
@RequiredArgsConstructor
public class NetworkController {

    private final NetworkAnalysisService networkAnalysisService;

    /** Feed a raw FIR / report; entities are extracted and merged into the graph. */
    @PostMapping("/ingest")
    public ResponseEntity<ExtractedEntitiesDto> ingest(@RequestBody IngestRequest request) {
        ExtractedEntitiesDto result = networkAnalysisService.ingestReport(
                request.getCaseNumber(), request.getRawText());
        return ResponseEntity.ok(result);
    }

    /** Manually (or CDR-analysis) confirmed link between two suspects. */
    @PostMapping("/link")
    public ResponseEntity<String> link(@RequestBody LinkRequest request) {
        networkAnalysisService.linkPersons(
                request.getPersonA(), request.getPersonB(),
                request.getRelationType(), request.getEvidenceSource(),
                request.getStrength());
        return ResponseEntity.ok("Relationship created");
    }

    /** Expand the network around a suspect (1-2 hops) — feeds the graph UI. */
    @GetMapping("/expand/{name}")
    public ResponseEntity<List<PersonNode>> expand(@PathVariable String name) {
        return ResponseEntity.ok(networkAnalysisService.getNetworkAround(name));
    }

    /** How are two suspects connected? */
    @GetMapping("/path")
    public ResponseEntity<List<PersonNode>> path(@RequestParam String personA,
                                                   @RequestParam String personB) {
        return ResponseEntity.ok(networkAnalysisService.findConnectionPath(personA, personB));
    }

    /** Ranked list of most "influential" / central suspects. */
    @GetMapping("/influencers")
    public ResponseEntity<List<PersonNode>> influencers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(networkAnalysisService.getTopInfluencers(limit));
    }

    @Data
    public static class IngestRequest {
        private String caseNumber;
        private String rawText;
    }

    @Data
    public static class LinkRequest {
        private String personA;
        private String personB;
        private String relationType;
        private String evidenceSource;
        private double strength;
    }
}
