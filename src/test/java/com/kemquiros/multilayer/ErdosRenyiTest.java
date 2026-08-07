package com.kemquiros.multilayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ErdosRenyiTest {

    private static final Layer UNDIRECTED = new Layer("Family", false);
    private static final Layer DIRECTED = new Layer("Friends", true);

    private static ErdosRenyi seeded(long seed) {
        return new ErdosRenyi(new SplittableRandom(seed));
    }

    @Test
    @DisplayName("the same seed gives the identical network")
    void sameSeedReproduces() {
        List<Agent> agents = ErdosRenyi.population(60);
        assertEquals(
                seeded(7L).generate(agents, UNDIRECTED, 0.2),
                seeded(7L).generate(agents, UNDIRECTED, 0.2));
    }

    @Test
    @DisplayName("different seeds give different networks")
    void differentSeedsDiffer() {
        List<Agent> agents = ErdosRenyi.population(60);
        assertNotEquals(
                seeded(7L).generate(agents, UNDIRECTED, 0.2),
                seeded(8L).generate(agents, UNDIRECTED, 0.2));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.05, 0.2, 0.5, 0.8})
    @DisplayName("the realised tie count matches its expectation")
    void tieCountMatchesExpectation(double probability) {
        int agents = 300;
        List<Agent> population = ErdosRenyi.population(agents);
        int realised = seeded(11L).generate(population, UNDIRECTED, probability).size();

        double expected = ErdosRenyi.expectedTies(agents, UNDIRECTED, probability);
        // A binomial with N = 44 850 trials: three standard deviations is a wide, and therefore
        // non-flaky, band, while still being far tighter than any wrong generator would fit.
        double deviation = Math.sqrt(UNDIRECTED.possibleTies(agents) * probability
                * (1 - probability));
        assertTrue(Math.abs(realised - expected) < 3 * deviation,
                "realised " + realised + " against expected " + expected);
    }

    @Test
    @DisplayName("p = 0 gives no ties and p = 1 gives every possible tie")
    void boundaryProbabilities() {
        List<Agent> agents = ErdosRenyi.population(40);
        assertTrue(seeded(1L).generate(agents, UNDIRECTED, 0.0).isEmpty());
        assertEquals(UNDIRECTED.possibleTies(40),
                seeded(1L).generate(agents, UNDIRECTED, 1.0).size());
        assertEquals(DIRECTED.possibleTies(40),
                seeded(1L).generate(agents, DIRECTED, 1.0).size());
    }

    @Test
    @DisplayName("a directed layer has twice the possible ties of an undirected one")
    void directedLayersHaveTwiceTheTies() {
        assertEquals(2 * UNDIRECTED.possibleTies(500), DIRECTED.possibleTies(500));
        assertEquals(0, UNDIRECTED.possibleTies(1));
        assertEquals(0, UNDIRECTED.possibleTies(0));
    }

    @Test
    @DisplayName("possibleTies does not overflow at sizes that overflow int")
    void possibleTiesUsesLongArithmetic() {
        // 100 000 agents give 4 999 950 000 possible ties, well past Integer.MAX_VALUE. In int
        // arithmetic this silently goes negative.
        assertTrue(UNDIRECTED.possibleTies(100_000) > Integer.MAX_VALUE);
        assertEquals(4_999_950_000L, UNDIRECTED.possibleTies(100_000));
    }

    @Test
    @DisplayName("on a directed layer both arcs of a pair can be realised independently")
    void directedArcsAreIndependent() {
        List<Tie> ties = seeded(3L).generate(ErdosRenyi.population(50), DIRECTED, 0.5);

        Set<Set<Agent>> pairs = new HashSet<>();
        int reciprocated = 0;
        for (Tie tie : ties) {
            if (!pairs.add(Set.of(tie.source(), tie.target()))) {
                reciprocated++;
            }
        }
        assertTrue(reciprocated > 0, "no pair was reciprocated, so the arcs are not independent");
        assertTrue(reciprocated < ties.size() / 2,
                "every pair was reciprocated, so the reverse arc is a mirror rather than a trial");
    }

    @Test
    @DisplayName("a probability outside [0, 1] is refused rather than silently clamped")
    void invalidProbabilityIsRefused() {
        List<Agent> agents = ErdosRenyi.population(10);
        assertThrows(IllegalArgumentException.class,
                () -> seeded(1L).generate(agents, UNDIRECTED, -0.1));
        assertThrows(IllegalArgumentException.class,
                () -> seeded(1L).generate(agents, UNDIRECTED, 1.1));
        assertThrows(IllegalArgumentException.class,
                () -> seeded(1L).generate(agents, UNDIRECTED, Double.NaN));
    }

    @Test
    @DisplayName("the 2015 probability formula underflows to exactly zero")
    void the2015FormulaUnderflows() {
        // p^m * (1-p)^(C(n,2)-m) for n = 100, p = 0.9. The exponent is about -699 in base 10,
        // and a double bottoms out near 1e-308. The 2015 code used this as the per-tie
        // probability, which would have produced a network with no ties at all -- and then
        // overwrote it with 0.5 on the next line, so no reported network ever used it.
        double p = 0.9;
        long pairs = 100L * 99L / 2L;
        double edges = pairs * p;
        double formula = Math.pow(p, edges) * Math.pow(1 - p, pairs - edges);

        assertEquals(0.0, formula, "the 2015 expression is expected to underflow");
        assertThrows(IllegalArgumentException.class,
                () -> ErdosRenyi.giantComponentThreshold(1));
    }

    @Test
    @DisplayName("the giant-component threshold is 1/(n-1)")
    void threshold() {
        assertEquals(1.0 / 99.0, ErdosRenyi.giantComponentThreshold(100), 1e-15);
        assertThrows(IllegalArgumentException.class, () -> ErdosRenyi.giantComponentThreshold(0));
    }
}
