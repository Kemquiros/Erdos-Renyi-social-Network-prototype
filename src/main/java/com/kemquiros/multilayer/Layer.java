package com.kemquiros.multilayer;

import java.util.Objects;

/**
 * One relation type in the network: family, work, friendship.
 *
 * <p>Each layer carries its own directedness, because the distinction changes how many potential
 * ties exist. On an <em>undirected</em> layer a tie between two people is one thing, so there are
 * {@code n(n-1)/2} possible ties. On a <em>directed</em> layer each direction is independent --
 * you may consider someone a friend without the reverse holding -- so there are {@code n(n-1)}.
 *
 * @param name a non-blank label
 * @param directed whether each direction of a tie is independent
 */
public record Layer(String name, boolean directed) {

    public Layer {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("a layer's name must not be blank");
        }
    }

    /**
     * The number of possible ties among {@code agents} people on this layer.
     *
     * @param agents the number of people, at least zero
     * @return {@code n(n-1)} when directed, {@code n(n-1)/2} otherwise
     * @throws IllegalArgumentException if {@code agents} is negative
     */
    public long possibleTies(int agents) {
        if (agents < 0) {
            throw new IllegalArgumentException("agents must not be negative, got " + agents);
        }
        // long throughout: at 100 000 agents an undirected layer already has 5e9 possible ties,
        // which overflows int silently and produces a negative count.
        long n = agents;
        long pairs = n * (n - 1) / 2;
        return directed ? pairs * 2 : pairs;
    }
}
