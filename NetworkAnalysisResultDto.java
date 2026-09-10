package com.sih.demo.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Single response model used by the JSP network-analysis dashboard.
 * It keeps extraction, graph statistics, paths, evidence and the
 * investigator-facing summary together so one ingest action can render
 * the complete analysis result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkAnalysisResultDto {

    private String caseNumber;
    private int personCount;
    private int phoneCount;
    private int locationCount;
    private int vehicleCount;

    private List<String> persons = new ArrayList<>();
    private List<String> phoneNumbers = new ArrayList<>();
    private List<String> locations = new ArrayList<>();
    private List<String> vehicleNumbers = new ArrayList<>();

    private int totalPersons;
    private int totalRelationships;

    private String keyPerson;
    private List<InfluencerRow> topInfluencers = new ArrayList<>();

    private String focusPerson;
    private List<RelationshipRow> evidence = new ArrayList<>();

    private String pathA;
    private String pathB;
    private List<String> pathResults = new ArrayList<>();

    private List<IndicatorRow> indicators = new ArrayList<>();

    private List<GraphNode> graphNodes = new ArrayList<>();
    private List<GraphEdge> graphEdges = new ArrayList<>();

    private String caseSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InfluencerRow {
        private int rank;
        private String name;
        private int degree;
        private double weightedScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationshipRow {
        private String source;
        private String target;
        private String relationType;
        private String evidenceSource;
        private double strength;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndicatorRow {
        private String level;
        private String title;
        private String detail;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphNode {
        private String name;
        private int x;
        private int y;
        private boolean key;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphEdge {
        private String source;
        private String target;
        private String relationType;
        private double strength;
        private int sourceX;
        private int sourceY;
        private int targetX;
        private int targetY;
    }
}
