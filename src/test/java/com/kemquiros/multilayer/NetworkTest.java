package com.kemquiros.multilayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NetworkTest {

    private static final Layer FAMILY = new Layer("Family", false);
    private static final Layer FRIENDS = new Layer("Friends", true);

    @Test
    @DisplayName("isolated agents appear in the degree map with degree zero")
    void isolatedAgentsAreCounted() {
        List<Agent> agents = ErdosRenyi.population(5);
        Network network = new Network(agents,
                Map.of(FAMILY, List.of(new Tie(agents.get(0), agents.get(1), FAMILY))));

        Map<Agent, Integer> degrees = network.degrees(FAMILY);
        assertEquals(5, degrees.size());
        assertEquals(0, degrees.get(agents.get(4)));
        // Mean over the population, not over the connected agents: 2/5, not 2/2.
        assertEquals(0.4, network.meanDegree(FAMILY), 1e-12);
    }

    @Test
    @DisplayName("mean degree approaches p(n-1) on an undirected layer")
    void meanDegreeMatchesTheory() {
        int agents = 400;
        double probability = 0.05;
        List<Agent> population = ErdosRenyi.population(agents);
        Network network = new Network(population, Map.of(FAMILY,
                new ErdosRenyi(new SplittableRandom(19L))
                        .generate(population, FAMILY, probability)));

        double expected = probability * (agents - 1);
        assertEquals(expected, network.meanDegree(FAMILY), 0.15 * expected);
    }

    @Test
    @DisplayName("the giant component appears above the threshold and not below it")
    void giantComponentTransition() {
        int agents = 1000;
        List<Agent> population = ErdosRenyi.population(agents);
        double threshold = ErdosRenyi.giantComponentThreshold(agents);

        Network sparse = new Network(population, Map.of(FAMILY,
                new ErdosRenyi(new SplittableRandom(5L))
                        .generate(population, FAMILY, threshold / 4)));
        Network dense = new Network(population, Map.of(FAMILY,
                new ErdosRenyi(new SplittableRandom(5L))
                        .generate(population, FAMILY, threshold * 4)));

        // Well below the threshold the largest component is logarithmic in n; well above it, a
        // constant fraction of the population. This is the sharpest statement the model makes,
        // and a generator that is subtly wrong will not reproduce it.
        assertTrue(sparse.largestComponent(FAMILY) < agents / 10,
                "largest component below threshold: " + sparse.largestComponent(FAMILY));
        assertTrue(dense.largestComponent(FAMILY) > agents / 2,
                "largest component above threshold: " + dense.largestComponent(FAMILY));
    }

    @Test
    @DisplayName("a complete layer is one component and an empty one is n components")
    void componentBoundaries() {
        List<Agent> agents = ErdosRenyi.population(30);
        ErdosRenyi generator = new ErdosRenyi(new SplittableRandom(1L));

        assertEquals(30, new Network(agents,
                Map.of(FAMILY, generator.generate(agents, FAMILY, 1.0)))
                .largestComponent(FAMILY));
        assertEquals(1, new Network(agents,
                Map.of(FAMILY, generator.generate(agents, FAMILY, 0.0)))
                .largestComponent(FAMILY));
    }

    @Test
    @DisplayName("reachability follows direction on a directed layer")
    void reachabilityRespectsDirection() {
        List<Agent> agents = ErdosRenyi.population(3);
        Network network = new Network(agents, Map.of(FRIENDS, List.of(
                new Tie(agents.get(0), agents.get(1), FRIENDS),
                new Tie(agents.get(1), agents.get(2), FRIENDS))));

        assertEquals(2, network.reachableFrom(agents.get(0), FRIENDS).size());
        // Nothing runs back the other way.
        assertTrue(network.reachableFrom(agents.get(2), FRIENDS).isEmpty());
    }

    @Test
    @DisplayName("reachability ignores direction on an undirected layer")
    void reachabilityIgnoresDirectionWhenUndirected() {
        List<Agent> agents = ErdosRenyi.population(3);
        Network network = new Network(agents, Map.of(FAMILY, List.of(
                new Tie(agents.get(0), agents.get(1), FAMILY),
                new Tie(agents.get(1), agents.get(2), FAMILY))));

        assertEquals(2, network.reachableFrom(agents.get(2), FAMILY).size());
    }

    @Test
    @DisplayName("an empty population is handled rather than dividing by zero")
    void emptyPopulation() {
        Network network = new Network(List.of(), Map.of(FAMILY, List.of()));
        assertEquals(0, network.largestComponent(FAMILY));
        assertTrue(Double.isNaN(network.meanDegree(FAMILY)));
    }

    @Test
    @DisplayName("agents are values, so identical ids are the same agent")
    void agentsAreValues() {
        // The 2015 Agente had no equals or hashCode, so two agents named "1" were different
        // objects and any set or map keyed on them would have held both.
        assertEquals(new Agent("1"), new Agent("1"));
        assertEquals(new Agent("1").hashCode(), new Agent("1").hashCode());
    }

    @Test
    @DisplayName("degenerate model values are refused")
    void degenerateValuesAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Agent(" "));
        assertThrows(IllegalArgumentException.class, () -> new Layer("", false));
        assertThrows(IllegalArgumentException.class,
                () -> new Tie(new Agent("1"), new Agent("1"), FAMILY));
        assertThrows(IllegalArgumentException.class, () -> FAMILY.possibleTies(-1));
    }
}
