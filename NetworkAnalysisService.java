package com.sih.demo.service;

import com.sih.demo.dto.ExtractedEntitiesDto;
import com.sih.demo.dto.NetworkAnalysisResultDto;
import com.sih.demo.entity.neo4j.*;
import com.sih.demo.repository.neo4j.PersonNodeRepository;
import com.sih.demo.repository.neo4j.PhoneNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NetworkAnalysisService {

    private final EntityExtractionService extractionService;
    private final PersonNodeRepository personRepository;
    private final PhoneNodeRepository phoneRepository;
    private final GraphCollectionsEngine graphCollectionsEngine;

    @Transactional
    public ExtractedEntitiesDto ingestReport(String caseNumber, String rawText) {
        ExtractedEntitiesDto entities = extractionService.extract(rawText);

        for (String personName : entities.getPersons()) {
            PersonNode person = personRepository.findByName(personName)
                    .orElseGet(() -> PersonNode.builder()
                            .name(personName)
                            .criminalRecordId(caseNumber)
                            .riskLevel("UNKNOWN")
                            .influenceScore(0.0)
                            .build());

            for (String number : entities.getPhoneNumbers()) {
                PhoneNode phone = phoneRepository.findByNumber(number)
                        .orElseGet(() -> phoneRepository.save(
                                PhoneNode.builder()
                                        .number(number)
                                        .status("UNKNOWN")
                                        .build()));
                person.getPhoneNumbers().add(phone);
            }

            for (String location : entities.getLocations()) {
                LocationNode locationNode = LocationNode.builder()
                        .name(location)
                        .locationType("MENTIONED_IN_REPORT")
                        .build();
                person.getLocations().add(locationNode);
            }

            personRepository.save(person);
            graphCollectionsEngine.addPerson(personName);
        }
        return entities;
    }

    @Transactional
    public void linkPersons(String nameA, String nameB, String relationType,
                            String evidenceSource, double strength) {
        PersonNode a = personRepository.findByName(nameA)
                .orElseGet(() -> personRepository.save(
                        PersonNode.builder().name(nameA).riskLevel("UNKNOWN").influenceScore(0.0).build()));
        PersonNode b = personRepository.findByName(nameB)
                .orElseGet(() -> personRepository.save(
                        PersonNode.builder().name(nameB).riskLevel("UNKNOWN").influenceScore(0.0).build()));

        double safeStrength = Math.max(0.0, Math.min(1.0, strength));
        AssociateRelationship rel = AssociateRelationship.builder()
                .target(b)
                .relationType(relationType == null || relationType.isBlank() ? "ASSOCIATE" : relationType)
                .evidenceSource(evidenceSource)
                .strength(safeStrength)
                .build();

        a.getAssociates().add(rel);
        personRepository.save(a);
        graphCollectionsEngine.addRelationship(nameA, nameB, safeStrength,
                relationType, evidenceSource);
    }


    /**
     * Build the complete dashboard result after a report is ingested.
     * Existing confirmed links are preserved. When a report contains
     * multiple extracted persons and no confirmed links exist yet, a small
     * chain of provisional REPORT_ASSOCIATION links is created so the demo
     * immediately has a traversable graph. These are clearly marked as
     * provisional in the UI and should be confirmed against evidence.
     */
    @Transactional
    public NetworkAnalysisResultDto analyzeAndPrepareDashboard(
            String caseNumber, String rawText) {

        ExtractedEntitiesDto extracted = ingestReport(caseNumber, rawText);
        List<String> persons = new ArrayList<>(extracted.getPersons());

        createProvisionalReportLinks(caseNumber, rawText, persons);

        List<Map.Entry<String, Integer>> degree =
                graphCollectionsEngine.topInfluencersByDegree(10);
        List<Map.Entry<String, Double>> weighted =
                graphCollectionsEngine.topInfluencersByWeightedScore(10);

        Map<String, Integer> degreeMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : degree) {
            degreeMap.put(entry.getKey(), entry.getValue());
        }

        List<NetworkAnalysisResultDto.InfluencerRow> influencers =
                new ArrayList<>();
        for (int i = 0; i < weighted.size(); i++) {
            Map.Entry<String, Double> entry = weighted.get(i);
            influencers.add(
                    NetworkAnalysisResultDto.InfluencerRow.builder()
                            .rank(i + 1)
                            .name(entry.getKey())
                            .degree(degreeMap.getOrDefault(entry.getKey(), 0))
                            .weightedScore(round(entry.getValue()))
                            .build());
        }

        String keyPerson = influencers.isEmpty()
                ? (persons.isEmpty() ? null : persons.get(0))
                : influencers.get(0).getName();

        String focusPerson = persons.isEmpty() ? keyPerson : persons.get(0);
        List<NetworkAnalysisResultDto.RelationshipRow> evidence =
                buildEvidenceRows(focusPerson);

        String pathA = persons.size() >= 1 ? persons.get(0) : null;
        String pathB = persons.size() >= 2 ? persons.get(persons.size() - 1) : pathA;
        List<String> pathResults = (pathA != null && pathB != null)
                ? graphCollectionsEngine.shortestPathBFS(pathA, pathB)
                : List.of();

        List<NetworkAnalysisResultDto.IndicatorRow> indicators =
                buildIndicators(persons, pathResults, keyPerson);

        List<NetworkAnalysisResultDto.GraphNode> graphNodes =
                buildGraphNodes(persons, keyPerson);
        List<NetworkAnalysisResultDto.GraphEdge> graphEdges =
                buildGraphEdges(graphNodes);

        String summary = buildCaseSummary(
                caseNumber, persons, extracted, keyPerson, pathResults);

        return NetworkAnalysisResultDto.builder()
                .caseNumber(caseNumber)
                .personCount(extracted.getPersons().size())
                .phoneCount(extracted.getPhoneNumbers().size())
                .locationCount(extracted.getLocations().size())
                .vehicleCount(extracted.getVehicleNumbers().size())
                .persons(persons)
                .phoneNumbers(new ArrayList<>(extracted.getPhoneNumbers()))
                .locations(new ArrayList<>(extracted.getLocations()))
                .vehicleNumbers(new ArrayList<>(extracted.getVehicleNumbers()))
                .totalPersons(graphCollectionsEngine.totalPersons())
                .totalRelationships(graphCollectionsEngine.totalRelationships())
                .keyPerson(keyPerson)
                .topInfluencers(influencers)
                .focusPerson(focusPerson)
                .evidence(evidence)
                .pathA(pathA)
                .pathB(pathB)
                .pathResults(pathResults)
                .indicators(indicators)
                .graphNodes(graphNodes)
                .graphEdges(graphEdges)
                .caseSummary(summary)
                .build();
    }

    private void createProvisionalReportLinks(
            String caseNumber, String rawText, List<String> persons) {

        if (persons.size() < 2) {
            return;
        }

        // Only create provisional links when the pair is not already linked.
        // The extracted order is used purely for demo graph continuity.
        for (int i = 0; i < persons.size() - 1; i++) {
            String a = persons.get(i);
            String b = persons.get(i + 1);

            boolean exists = graphCollectionsEngine.getDirectAssociates(a)
                    .stream()
                    .anyMatch(edge -> edge.target().equalsIgnoreCase(b));

            if (!exists) {
                linkPersons(
                        a,
                        b,
                        "REPORT_ASSOCIATION",
                        "REPORT-" + caseNumber,
                        0.50
                );
            }
        }
    }

    private List<NetworkAnalysisResultDto.RelationshipRow> buildEvidenceRows(String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }

        List<NetworkAnalysisResultDto.RelationshipRow> rows = new ArrayList<>();
        for (GraphCollectionsEngine.Edge edge :
                graphCollectionsEngine.getDirectAssociates(name)) {
            rows.add(NetworkAnalysisResultDto.RelationshipRow.builder()
                    .source(name)
                    .target(edge.target())
                    .relationType(edge.relationType())
                    .evidenceSource(edge.evidenceSource())
                    .strength(round(edge.weight()))
                    .build());
        }
        return rows;
    }

    private List<NetworkAnalysisResultDto.IndicatorRow> buildIndicators(
            List<String> persons, List<String> path, String keyPerson) {

        List<NetworkAnalysisResultDto.IndicatorRow> result = new ArrayList<>();

        if (persons.size() >= 2) {
            result.add(NetworkAnalysisResultDto.IndicatorRow.builder()
                    .level("INFO")
                    .title("Multi-person network detected")
                    .detail(persons.size() + " persons were extracted from the report.")
                    .build());
        }

        if (path.size() > 2) {
            result.add(NetworkAnalysisResultDto.IndicatorRow.builder()
                    .level("HIGH")
                    .title("Indirect connection detected")
                    .detail("A path of " + (path.size() - 1) + " relationship hop(s) was found between "
                            + path.get(0) + " and " + path.get(path.size() - 1) + ".")
                    .build());
        }

        if (keyPerson != null) {
            result.add(NetworkAnalysisResultDto.IndicatorRow.builder()
                    .level("REVIEW")
                    .title("Priority actor")
                    .detail(keyPerson + " ranks highest by weighted relationship score in the current graph.")
                    .build());
        }

        result.add(NetworkAnalysisResultDto.IndicatorRow.builder()
                .level("VERIFY")
                .title("Evidence verification required")
                .detail("REPORT_ASSOCIATION links are provisional analytical leads and should be confirmed against source evidence.")
                .build());

        return result;
    }

    private List<NetworkAnalysisResultDto.GraphNode> buildGraphNodes(
            List<String> persons, String keyPerson) {

        List<NetworkAnalysisResultDto.GraphNode> nodes = new ArrayList<>();
        if (persons.isEmpty()) {
            return nodes;
        }

        int centerX = 420;
        int centerY = 210;
        int radiusX = 290;
        int radiusY = 135;

        for (int i = 0; i < persons.size(); i++) {
            double angle = persons.size() == 1
                    ? 0
                    : (2.0 * Math.PI * i / persons.size()) - Math.PI / 2.0;
            int x = centerX + (int) Math.round(radiusX * Math.cos(angle));
            int y = centerY + (int) Math.round(radiusY * Math.sin(angle));
            String name = persons.get(i);
            nodes.add(NetworkAnalysisResultDto.GraphNode.builder()
                    .name(name)
                    .x(x)
                    .y(y)
                    .key(name.equalsIgnoreCase(keyPerson))
                    .build());
        }
        return nodes;
    }

    private List<NetworkAnalysisResultDto.GraphEdge> buildGraphEdges(
            List<NetworkAnalysisResultDto.GraphNode> nodes) {

        Map<String, NetworkAnalysisResultDto.GraphNode> nodeMap = new HashMap<>();
        for (NetworkAnalysisResultDto.GraphNode node : nodes) {
            nodeMap.put(node.getName().toLowerCase(Locale.ROOT), node);
        }

        Set<String> seen = new HashSet<>();
        List<NetworkAnalysisResultDto.GraphEdge> edges = new ArrayList<>();

        for (NetworkAnalysisResultDto.GraphNode node : nodes) {
            for (GraphCollectionsEngine.Edge edge :
                    graphCollectionsEngine.getDirectAssociates(node.getName())) {

                NetworkAnalysisResultDto.GraphNode target =
                        nodeMap.get(edge.target().toLowerCase(Locale.ROOT));
                if (target == null) {
                    continue;
                }

                String key1 = node.getName().toLowerCase(Locale.ROOT);
                String key2 = target.getName().toLowerCase(Locale.ROOT);
                String edgeKey = key1.compareTo(key2) < 0
                        ? key1 + "|" + key2 + "|" + edge.relationType()
                        : key2 + "|" + key1 + "|" + edge.relationType();

                if (!seen.add(edgeKey)) {
                    continue;
                }

                edges.add(NetworkAnalysisResultDto.GraphEdge.builder()
                        .source(node.getName())
                        .target(target.getName())
                        .relationType(edge.relationType())
                        .strength(round(edge.weight()))
                        .sourceX(node.getX())
                        .sourceY(node.getY())
                        .targetX(target.getX())
                        .targetY(target.getY())
                        .build());
            }
        }

        return edges;
    }

    private String buildCaseSummary(
            String caseNumber, List<String> persons, ExtractedEntitiesDto extracted,
            String keyPerson, List<String> path) {

        StringBuilder sb = new StringBuilder();
        sb.append("Case ").append(caseNumber)
                .append(" produced ").append(persons.size())
                .append(" extracted person(s), ")
                .append(extracted.getPhoneNumbers().size()).append(" phone(s), ")
                .append(extracted.getLocations().size()).append(" location(s), and ")
                .append(extracted.getVehicleNumbers().size()).append(" vehicle number(s). ");

        if (keyPerson != null) {
            sb.append(keyPerson)
                    .append(" currently ranks highest by weighted relationship score. ");
        }

        if (path.size() > 1) {
            sb.append("The shortest current graph path is ")
                    .append(String.join(" → ", path)).append(". ");
        }

        sb.append("These are analytical leads from the supplied synthetic/demo data and require independent evidence verification.");
        return sb.toString();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public List<PersonNode> getNetworkAround(String name) {
        return personRepository.findNetworkAround(name);
    }

    public List<PersonNode> findConnectionPath(String nameA, String nameB) {
        return personRepository.findPersonsOnShortestPath(nameA, nameB);
    }

    public List<PersonNode> getTopInfluencers(int limit) {
        return personRepository.findTopInfluencersByDegree(limit);
    }
}
