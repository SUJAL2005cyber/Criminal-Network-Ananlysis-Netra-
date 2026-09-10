
package com.sih.demo.service;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

@Component
public class GraphCollectionsEngine {

    /*
     * Adjacency list:
     *
     * Person -> List of connected persons
     */
    private final Map<String, List<Edge>> adjacencyList =
            new HashMap<>();


    /*
     * Graph edge.
     */
    public record Edge(
            String target,
            double weight,
            String relationType,
            String evidenceSource
    ) {
    }


    /*
     * Add a person to the graph.
     */
    public synchronized void addPerson(String name) {

        String normalizedName = normalizeName(name);

        if (normalizedName == null) {
            return;
        }

        adjacencyList.computeIfAbsent(
                normalizedName,
                key -> new ArrayList<>()
        );
    }


    /*
     * Add or update a relationship.
     *
     * The graph is undirected:
     *
     * A -> B
     * B -> A
     */
    public synchronized void addRelationship(
            String personA,
            String personB,
            double weight,
            String relationType,
            String evidenceSource) {

        String source = normalizeName(personA);
        String target = normalizeName(personB);

        if (source == null || target == null) {
            return;
        }

        /*
         * Ignore self relationships.
         */
        if (source.equalsIgnoreCase(target)) {
            return;
        }

        String normalizedRelationType =
                normalizeRelationType(relationType);

        String normalizedEvidence =
                normalizeOptional(evidenceSource);

        double normalizedWeight =
                normalizeWeight(weight);


        /*
         * Make sure both persons exist.
         */
        addPerson(source);
        addPerson(target);


        /*
         * Remove previous relationship having the same type.
         */
        adjacencyList.get(source).removeIf(
                edge ->
                        edge.target().equalsIgnoreCase(target)
                                &&
                        edge.relationType()
                                .equalsIgnoreCase(
                                        normalizedRelationType
                                )
        );


        adjacencyList.get(target).removeIf(
                edge ->
                        edge.target().equalsIgnoreCase(source)
                                &&
                        edge.relationType()
                                .equalsIgnoreCase(
                                        normalizedRelationType
                                )
        );


        /*
         * Add source -> target.
         */
        adjacencyList
                .get(source)
                .add(
                        new Edge(
                                target,
                                normalizedWeight,
                                normalizedRelationType,
                                normalizedEvidence
                        )
                );


        /*
         * Add target -> source.
         */
        adjacencyList
                .get(target)
                .add(
                        new Edge(
                                source,
                                normalizedWeight,
                                normalizedRelationType,
                                normalizedEvidence
                        )
                );
    }


    /*
     * Breadth First Search.
     *
     * Finds the path having the minimum
     * number of relationship hops.
     */
    public synchronized List<String> shortestPathBFS(
            String start,
            String end) {

        String source = normalizeName(start);
        String target = normalizeName(end);

        if (source == null || target == null) {
            return Collections.emptyList();
        }

        if (!containsPerson(source)
                || !containsPerson(target)) {

            return Collections.emptyList();
        }

        if (source.equalsIgnoreCase(target)) {
            return List.of(source);
        }


        Map<String, String> parent =
                new HashMap<>();

        Set<String> visited =
                new HashSet<>();

        Queue<String> queue =
                new ArrayDeque<>();


        queue.offer(source);
        visited.add(source);


        while (!queue.isEmpty()) {

            String current =
                    queue.poll();


            List<Edge> edges =
                    adjacencyList.getOrDefault(
                            current,
                            Collections.emptyList()
                    );


            for (Edge edge : edges) {

                String next =
                        edge.target();


                if (!visited.add(next)) {
                    continue;
                }


                parent.put(
                        next,
                        current
                );


                if (next.equalsIgnoreCase(target)) {

                    return reconstructPath(
                            parent,
                            source,
                            next
                    );
                }


                queue.offer(next);
            }
        }


        return Collections.emptyList();
    }


    /*
     * Strongest relationship path.
     *
     * Uses Dijkstra-style shortest-path logic.
     *
     * Higher relationship strength
     * means lower traversal cost.
     */
    public synchronized List<String> strongestConnectionPath(
            String start,
            String end) {

        String source = normalizeName(start);
        String target = normalizeName(end);

        if (source == null || target == null) {
            return Collections.emptyList();
        }

        if (!containsPerson(source)
                || !containsPerson(target)) {

            return Collections.emptyList();
        }

        if (source.equalsIgnoreCase(target)) {
            return List.of(source);
        }


        Map<String, Double> distance =
                new HashMap<>();


        Map<String, String> parent =
                new HashMap<>();


        PriorityQueue<NodeDistance> queue =
                new PriorityQueue<>(
                        Comparator.comparingDouble(
                                NodeDistance::distance
                        )
                );


        /*
         * Initialize all distances.
         */
        for (String node :
                adjacencyList.keySet()) {

            distance.put(
                    node,
                    Double.POSITIVE_INFINITY
            );
        }


        /*
         * Starting node.
         */
        distance.put(
                source,
                0.0
        );


        queue.offer(
                new NodeDistance(
                        source,
                        0.0
                )
        );


        while (!queue.isEmpty()) {

            NodeDistance current =
                    queue.poll();


            String currentNode =
                    current.node();


            double currentDistance =
                    current.distance();


            double knownDistance =
                    distance.getOrDefault(
                            currentNode,
                            Double.POSITIVE_INFINITY
                    );


            /*
             * Ignore outdated queue entries.
             */
            if (currentDistance > knownDistance) {
                continue;
            }


            /*
             * Destination reached.
             */
            if (currentNode.equalsIgnoreCase(target)) {
                break;
            }


            List<Edge> edges =
                    adjacencyList.getOrDefault(
                            currentNode,
                            Collections.emptyList()
                    );


            for (Edge edge : edges) {

                /*
                 * Stronger relationship =
                 * lower cost.
                 */
                double cost =
                        1.0 - edge.weight();


                /*
                 * Avoid zero-cost edges.
                 */
                cost =
                        Math.max(
                                0.01,
                                cost
                        );


                double candidateDistance =
                        currentDistance + cost;


                double existingDistance =
                        distance.getOrDefault(
                                edge.target(),
                                Double.POSITIVE_INFINITY
                        );


                if (candidateDistance
                        < existingDistance) {

                    distance.put(
                            edge.target(),
                            candidateDistance
                    );


                    parent.put(
                            edge.target(),
                            currentNode
                    );


                    queue.offer(
                            new NodeDistance(
                                    edge.target(),
                                    candidateDistance
                            )
                    );
                }
            }
        }


        double finalDistance =
                distance.getOrDefault(
                        target,
                        Double.POSITIVE_INFINITY
                );


        if (Double.isInfinite(finalDistance)) {
            return Collections.emptyList();
        }


        return reconstructPath(
                parent,
                source,
                target
        );
    }


    /*
     * Reconstruct path from parent map.
     */
    private List<String> reconstructPath(
            Map<String, String> parent,
            String start,
            String end) {

        LinkedList<String> path =
                new LinkedList<>();


        String current = end;


        while (current != null) {

            path.addFirst(current);


            if (current.equalsIgnoreCase(start)) {
                return path;
            }


            current =
                    parent.get(current);
        }


        return Collections.emptyList();
    }


    /*
     * Rank persons by number of
     * direct connections.
     */
    public synchronized List<Map.Entry<String, Integer>>
    topInfluencersByDegree(int limit) {

        if (limit <= 0 || adjacencyList.isEmpty()) {
            return Collections.emptyList();
        }


        List<Map.Entry<String, Integer>> ranked =
                new ArrayList<>();


        for (Map.Entry<String, List<Edge>> entry :
                adjacencyList.entrySet()) {

            ranked.add(
                    Map.entry(
                            entry.getKey(),
                            entry.getValue().size()
                    )
            );
        }


        /*
         * IMPORTANT:
         *
         * Explicit comparator types avoid
         * Java generic type inference errors.
         */
        ranked.sort(
                Comparator
                        .comparingInt(
                                (Map.Entry<String, Integer> entry)
                                        -> entry.getValue()
                        )
                        .reversed()
                        .thenComparing(
                                (Map.Entry<String, Integer> entry)
                                        -> entry.getKey(),
                                String.CASE_INSENSITIVE_ORDER
                        )
        );


        return new ArrayList<>(
                ranked.subList(
                        0,
                        Math.min(
                                limit,
                                ranked.size()
                        )
                )
        );
    }


    /*
     * Rank persons by the total strength
     * of their relationships.
     */
    public synchronized List<Map.Entry<String, Double>>
    topInfluencersByWeightedScore(int limit) {

        if (limit <= 0 || adjacencyList.isEmpty()) {
            return Collections.emptyList();
        }


        Map<String, Double> scores =
                new HashMap<>();


        for (Map.Entry<String, List<Edge>> entry :
                adjacencyList.entrySet()) {

            double total =
                    entry.getValue()
                            .stream()
                            .mapToDouble(
                                    Edge::weight
                            )
                            .sum();


            scores.put(
                    entry.getKey(),
                    total
            );
        }


        List<Map.Entry<String, Double>> ranked =
                new ArrayList<>(
                        scores.entrySet()
                );


        /*
         * IMPORTANT:
         *
         * Explicit lambda types prevent
         * Comparator generic inference problems.
         */
        ranked.sort(
                Comparator
                        .comparingDouble(
                                (Map.Entry<String, Double> entry)
                                        -> entry.getValue()
                        )
                        .reversed()
                        .thenComparing(
                                (Map.Entry<String, Double> entry)
                                        -> entry.getKey(),
                                String.CASE_INSENSITIVE_ORDER
                        )
        );


        return new ArrayList<>(
                ranked.subList(
                        0,
                        Math.min(
                                limit,
                                ranked.size()
                        )
                )
        );
    }


    /*
     * Return direct associates.
     */
    public synchronized List<Edge>
    getDirectAssociates(String name) {

        String normalizedName =
                normalizeName(name);


        if (normalizedName == null) {
            return Collections.emptyList();
        }


        return new ArrayList<>(
                adjacencyList.getOrDefault(
                        normalizedName,
                        Collections.emptyList()
                )
        );
    }


    /*
     * Total persons in the graph.
     */
    public synchronized int totalPersons() {

        return adjacencyList.size();
    }


    /*
     * Total unique relationships.
     *
     * Every relationship is stored twice:
     *
     * A -> B
     * B -> A
     */
    public synchronized int totalRelationships() {

        int totalDirectedEdges =
                adjacencyList.values()
                        .stream()
                        .mapToInt(List::size)
                        .sum();


        return totalDirectedEdges / 2;
    }


    /*
     * Check whether a person exists.
     */
    public synchronized boolean containsPerson(
            String name) {

        String normalizedName =
                normalizeName(name);


        if (normalizedName == null) {
            return false;
        }


        return adjacencyList.keySet()
                .stream()
                .anyMatch(
                        existing ->
                                existing.equalsIgnoreCase(
                                        normalizedName
                                )
                );
    }


    /*
     * Clear in-memory graph.
     *
     * Useful during development/testing.
     */
    public synchronized void clearGraph() {

        adjacencyList.clear();
    }


    /*
     * Normalize person name.
     */
    private String normalizeName(
            String name) {

        if (name == null) {
            return null;
        }


        String normalized =
                name.trim();


        if (normalized.isBlank()) {
            return null;
        }


        return normalized;
    }


    /*
     * Normalize optional text.
     */
    private String normalizeOptional(
            String value) {

        if (value == null) {
            return null;
        }


        String normalized =
                value.trim();


        if (normalized.isBlank()) {
            return null;
        }


        return normalized;
    }


    /*
     * Normalize relationship type.
     */
    private String normalizeRelationType(
            String relationType) {

        if (relationType == null
                || relationType.isBlank()) {

            return "ASSOCIATE";
        }


        return relationType
                .trim()
                .toUpperCase(Locale.ROOT);
    }


    /*
     * Normalize relationship weight.
     */
    private double normalizeWeight(
            double weight) {

        if (Double.isNaN(weight)
                || Double.isInfinite(weight)) {

            return 0.0;
        }


        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        weight
                )
        );
    }


    /*
     * Internal node used by priority queue.
     */
    private record NodeDistance(
            String node,
            double distance
    ) {
    }
}

