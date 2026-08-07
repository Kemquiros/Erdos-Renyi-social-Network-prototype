package com.kemquiros.multilayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Generates Erdos-Renyi random graphs in the {@code G(n, p)} model.
 *
 * <p>Every possible tie exists independently with probability {@code p}. That is the whole model,
 * and its simplicity is what makes its properties provable:
 *
 * <ul>
 *   <li>the expected number of ties is {@code p} times the number of possible ties;
 *   <li>each agent's degree is binomially distributed with mean {@code p(n-1)};
 *   <li>a giant connected component appears abruptly at {@code p = 1/(n-1)} -- below it the
 *       largest component is logarithmic in {@code n}, above it a constant fraction of the whole.
 * </ul>
 *
 * <p>All three are asserted in the test suite rather than described, because a generator that is
 * subtly wrong still produces plausible-looking graphs.
 *
 * <h2>The bug this class replaces</h2>
 *
 * <p>The 2015 {@code generarParametroErdos} computed
 * {@code p^m * (1-p)^(C(n,2)-m)} -- the probability of one specific labelled graph having exactly
 * {@code m} edges -- and used it as the per-tie probability. Those are different quantities, and
 * the arithmetic makes the confusion moot: for {@code n = 100} and {@code p = 0.9} the expression
 * evaluates to about {@code 1e-699}, which underflows a {@code double} to exactly zero. A network
 * generated with that probability has no ties at all.
 *
 * <p>The method then ended with {@code probabilidad = 0.5;}, discarding the computation entirely
 * and generating every reported network at one half. Every printed value above that line was
 * dead. The parameter the program was named after was never used.
 */
public final class ErdosRenyi {

    private final RandomGenerator random;

    /**
     * @param random the source of randomness; pass a seeded generator to make a run reproducible
     */
    public ErdosRenyi(RandomGenerator random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    /**
     * Realise each possible tie on {@code layer} independently with probability {@code p}.
     *
     * @param agents the population
     * @param layer the relation to generate
     * @param probability the per-tie probability, in {@code [0, 1]}
     * @return the ties that were realised, in a stable order
     * @throws IllegalArgumentException if {@code probability} is outside {@code [0, 1]} or not a
     *     number -- the 2015 code silently accepted an underflowed zero
     */
    public List<Tie> generate(List<Agent> agents, Layer layer, double probability) {
        Objects.requireNonNull(agents, "agents");
        Objects.requireNonNull(layer, "layer");
        if (Double.isNaN(probability) || probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException(
                    "probability must lie in [0, 1], got " + probability);
        }

        List<Tie> ties = new ArrayList<>();
        for (int i = 0; i < agents.size(); i++) {
            for (int j = i + 1; j < agents.size(); j++) {
                if (random.nextDouble() < probability) {
                    ties.add(new Tie(agents.get(i), agents.get(j), layer));
                }
                // On a directed layer the reverse arc is a separate trial, not a mirror of the
                // first: A may name B a friend without B naming A.
                if (layer.directed() && random.nextDouble() < probability) {
                    ties.add(new Tie(agents.get(j), agents.get(i), layer));
                }
            }
        }
        return List.copyOf(ties);
    }

    /**
     * The probability at which a giant connected component appears.
     *
     * <p>Below {@code 1/(n-1)} the largest component has {@code O(log n)} agents; above it, a
     * constant fraction of them. The transition is sharp, and it is the single most quoted
     * property of the model.
     *
     * @param agents the number of agents, at least two
     * @return the critical per-tie probability
     * @throws IllegalArgumentException if fewer than two agents
     */
    public static double giantComponentThreshold(int agents) {
        if (agents < 2) {
            throw new IllegalArgumentException("need at least two agents, got " + agents);
        }
        return 1.0 / (agents - 1);
    }

    /**
     * The expected number of ties on a layer at a given probability.
     *
     * @param agents the number of agents
     * @param layer the relation
     * @param probability the per-tie probability
     * @return the expectation, which is exact rather than estimated
     */
    public static double expectedTies(int agents, Layer layer, double probability) {
        return layer.possibleTies(agents) * probability;
    }

    /** Convenience: {@code n} agents named {@code "1"} through {@code "n"}. */
    public static List<Agent> population(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative, got " + size);
        }
        List<Agent> agents = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            agents.add(new Agent(Integer.toString(i)));
        }
        return List.copyOf(agents);
    }
}
