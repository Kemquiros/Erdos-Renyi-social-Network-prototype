package com.kemquiros.multilayer;

import java.util.Objects;

/**
 * A person in the network.
 *
 * <p>A record rather than a class with a single getter: two agents with the same identifier are
 * the same agent, which is what {@code equals} and {@code hashCode} need to say for agents to be
 * usable as map keys. The 2015 {@code Agente} had neither, so {@code Set<Agente>} would have held
 * duplicates and any degree count keyed on agents would have been wrong.
 *
 * @param id a non-blank identifier, unique within a network
 */
public record Agent(String id) {

    public Agent {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("an agent's id must not be blank");
        }
    }

    @Override
    public String toString() {
        return id;
    }
}
