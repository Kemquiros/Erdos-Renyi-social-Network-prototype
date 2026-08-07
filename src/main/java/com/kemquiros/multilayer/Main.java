package com.kemquiros.multilayer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

/**
 * Builds the three-layer social network the 2015 prototype described, and reports it.
 *
 * <pre>
 * java -jar multilayer.jar [agents] [probability] [seed]
 * </pre>
 *
 * <p>All three were compile-time constants in 2015, and the probability was overwritten with 0.5
 * regardless of what was computed.
 */
public final class Main {

    private Main() {}

    public static void main(String[] args) {
        int agentCount = args.length > 0 ? Integer.parseInt(args[0]) : 100;
        double probability = args.length > 1 ? Double.parseDouble(args[1]) : 0.05;
        long seed = args.length > 2 ? Long.parseLong(args[2]) : 20150429L;

        List<Layer> layers = List.of(
                new Layer("Family", false),
                new Layer("Work", false),
                new Layer("Friends", true));

        List<Agent> agents = ErdosRenyi.population(agentCount);
        // SplittableRandom rather than Random: it implements RandomGenerator, takes a seed, and
        // has far better statistical properties than Random's 48-bit linear congruential
        // generator -- which matters when the whole program is a sequence of Bernoulli trials.
        ErdosRenyi generator = new ErdosRenyi(new SplittableRandom(seed));

        Map<Layer, List<Tie>> ties = new LinkedHashMap<>();
        layers.forEach(layer -> ties.put(layer, generator.generate(agents, layer, probability)));
        Network network = new Network(agents, ties);

        System.out.printf("%d agents, p = %.4f, seed = %d%n", agentCount, probability, seed);
        System.out.printf("giant-component threshold p* = %.6f%n",
                ErdosRenyi.giantComponentThreshold(agentCount));
        System.out.println();
        System.out.printf("%-10s %10s %10s %10s %12s %14s%n",
                "layer", "directed", "possible", "realised", "expected", "mean degree");

        for (Layer layer : layers) {
            System.out.printf("%-10s %10s %10d %10d %12.1f %14.3f%n",
                    layer.name(),
                    layer.directed(),
                    layer.possibleTies(agentCount),
                    network.ties(layer).size(),
                    ErdosRenyi.expectedTies(agentCount, layer, probability),
                    network.meanDegree(layer));
        }

        System.out.println();
        for (Layer layer : layers) {
            System.out.printf("largest component on %-8s %6d of %d agents%n",
                    layer.name(), network.largestComponent(layer), agentCount);
        }
    }
}
