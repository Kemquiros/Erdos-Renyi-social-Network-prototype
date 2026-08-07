package com.kemquiros.multilayer;

import java.util.Objects;

/**
 * A realised connection from one agent to another on a given layer.
 *
 * <p>Only ties that <em>exist</em> are represented. The 2015 code materialised every possible
 * connection as an object with an {@code esActiva} flag and then flipped the flags -- 19 800
 * objects for a 100-agent network, of which the interesting ones were a subset. Holding only the
 * realised ties makes the memory cost proportional to the answer rather than to the search space,
 * which is what lets this run at network sizes the original could not reach.
 *
 * @param source the agent the tie runs from
 * @param target the agent the tie runs to
 * @param layer the relation the tie belongs to
 */
public record Tie(Agent source, Agent target, Layer layer) {

    public Tie {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(layer, "layer");
        if (source.equals(target)) {
            throw new IllegalArgumentException("an agent cannot be tied to itself: " + source);
        }
    }

    @Override
    public String toString() {
        return source + (layer.directed() ? " -> " : " -- ") + target + " [" + layer.name() + "]";
    }
}
