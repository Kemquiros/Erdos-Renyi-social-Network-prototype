package com.kemquiros.multilayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A multilayer network: one population, several relations over it.
 *
 * <p>The point of the multilayer form is that the same people are tied differently depending on
 * the relation. Family ties are undirected and sparse; friendship is directed and may be
 * one-sided. Measuring a layer in isolation and measuring the union give different answers, and
 * both are available here.
 */
public final class Network {

    private final List<Agent> agents;
    private final Map<Layer, List<Tie>> tiesByLayer;

    public Network(List<Agent> agents, Map<Layer, List<Tie>> tiesByLayer) {
        this.agents = List.copyOf(Objects.requireNonNull(agents, "agents"));
        this.tiesByLayer = Map.copyOf(Objects.requireNonNull(tiesByLayer, "tiesByLayer"));
    }

    public List<Agent> agents() {
        return agents;
    }

    public Set<Layer> layers() {
        return tiesByLayer.keySet();
    }

    /**
     * The ties on one layer.
     *
     * @param layer the relation
     * @return the realised ties, or an empty list if the layer is not present
     */
    public List<Tie> ties(Layer layer) {
        return tiesByLayer.getOrDefault(layer, List.of());
    }

    /** Every tie on every layer. */
    public List<Tie> allTies() {
        List<Tie> all = new ArrayList<>();
        tiesByLayer.values().forEach(all::addAll);
        return List.copyOf(all);
    }

    /**
     * How many ties each agent has on a layer, counting both directions.
     *
     * @param layer the relation
     * @return degree by agent, with zero entries for isolated agents -- the 2015 code would have
     *     omitted them, so an average degree computed from it would have been the average over
     *     <em>connected</em> agents rather than over the population
     */
    public Map<Agent, Integer> degrees(Layer layer) {
        Map<Agent, Integer> degree = new HashMap<>();
        agents.forEach(agent -> degree.put(agent, 0));
        for (Tie tie : ties(layer)) {
            degree.merge(tie.source(), 1, Integer::sum);
            degree.merge(tie.target(), 1, Integer::sum);
        }
        return Map.copyOf(degree);
    }

    /** The mean degree on a layer, over the whole population. */
    public double meanDegree(Layer layer) {
        if (agents.isEmpty()) {
            return Double.NaN;
        }
        return degrees(layer).values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    /**
     * The size of the largest connected component on a layer, ignoring direction.
     *
     * <p>Computed by union-find. This is the quantity that jumps at the giant-component
     * threshold, and it is the reason that threshold is worth knowing.
     *
     * @param layer the relation
     * @return the number of agents in the largest component; 0 for an empty population
     */
    public int largestComponent(Layer layer) {
        if (agents.isEmpty()) {
            return 0;
        }
        Map<Agent, Agent> parent = new HashMap<>();
        agents.forEach(agent -> parent.put(agent, agent));

        for (Tie tie : ties(layer)) {
            Agent left = find(parent, tie.source());
            Agent right = find(parent, tie.target());
            if (!left.equals(right)) {
                parent.put(left, right);
            }
        }

        Map<Agent, Integer> sizes = new HashMap<>();
        for (Agent agent : agents) {
            sizes.merge(find(parent, agent), 1, Integer::sum);
        }
        return sizes.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    /** Agents reachable from {@code start} on {@code layer}, following direction. */
    public Set<Agent> reachableFrom(Agent start, Layer layer) {
        Objects.requireNonNull(start, "start");
        Map<Agent, List<Agent>> outgoing = new HashMap<>();
        for (Tie tie : ties(layer)) {
            outgoing.computeIfAbsent(tie.source(), key -> new ArrayList<>()).add(tie.target());
            if (!layer.directed()) {
                outgoing.computeIfAbsent(tie.target(), key -> new ArrayList<>()).add(tie.source());
            }
        }

        Set<Agent> seen = new HashSet<>();
        List<Agent> frontier = new ArrayList<>(List.of(start));
        while (!frontier.isEmpty()) {
            Agent current = frontier.remove(frontier.size() - 1);
            if (seen.add(current)) {
                frontier.addAll(outgoing.getOrDefault(current, List.of()));
            }
        }
        seen.remove(start);
        return Set.copyOf(seen);
    }

    private static Agent find(Map<Agent, Agent> parent, Agent agent) {
        Agent root = agent;
        while (!parent.get(root).equals(root)) {
            root = parent.get(root);
        }
        // Path compression: without it a chain of n agents makes find linear and the whole
        // computation quadratic, which is visible at the network sizes this now reaches.
        Agent current = agent;
        while (!parent.get(current).equals(root)) {
            Agent next = parent.get(current);
            parent.put(current, root);
            current = next;
        }
        return root;
    }
}
