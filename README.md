# multilayer-erdos-renyi — random graphs over a layered social network

[![CI](https://github.com/Kemquiros/Erdos-Renyi-social-Network-prototype/actions/workflows/ci.yml/badge.svg)](https://github.com/Kemquiros/Erdos-Renyi-social-Network-prototype/actions/workflows/ci.yml)
[![Java 21+](https://img.shields.io/badge/java-21%2B-blue)](https://openjdk.org/)
[![License: MIT](https://img.shields.io/badge/license-MIT-green)](LICENSE)

People are tied to each other in more than one way at once — family, work, friendship — and the
same pair may be connected on one layer and not another. This generates such a network under the
Erdős–Rényi model, where each possible tie exists independently with probability `p`, and measures
the properties the model is famous for.

If you came to **use** it, start at [Run it](#run-it).
If you came to **learn what Erdős–Rényi actually predicts**, start at
[The model](#the-model) — every claim there is asserted against its analytic value in the test
suite.

Written in **2015** at **Universidad de Antioquia**, Medellín, by **John Edisson Tapias
Zarrazola**. The original NetBeans prototype is preserved in
[`docs/netbeans-2015/`](docs/netbeans-2015/).

---

## Run it

```bash
mvn verify
java -jar target/multilayer-erdos-renyi-1.0.0.jar 200 0.02 42
```

```
200 agents, p = 0.0200, seed = 42
giant-component threshold p* = 0.005025

layer        directed   possible   realised     expected    mean degree
Family          false      19900        ...        398.0            ...
Work            false      19900        ...        398.0            ...
Friends          true      39800        ...        796.0            ...
```

`agents`, `probability` and `seed` were all compile-time constants in 2015 — and the probability
was overwritten regardless of what was computed. See [The bug](#the-bug).

Or use it as a library:

```java
List<Agent> people = ErdosRenyi.population(1000);
Layer friends = new Layer("Friends", true);          // directed: A may name B without the reverse

var generator = new ErdosRenyi(new SplittableRandom(42));   // seeded, so the run reproduces
Network network = new Network(people, Map.of(friends, generator.generate(people, friends, 0.01)));

network.meanDegree(friends);          // ~ p(n-1) on an undirected layer
network.largestComponent(friends);    // jumps at p = 1/(n-1)
network.reachableFrom(people.get(0), friends);
```

---

## The model

`G(n, p)`: take `n` people, and let each possible tie exist independently with probability `p`.
That is the entire model, and its simplicity is exactly why its behaviour can be *derived* rather
than only observed.

**Expected ties.** On an undirected layer there are `n(n−1)/2` possible ties, so the expected
number realised is

$$ \mathbb{E}[m] = p\,\frac{n(n-1)}{2} $$

On a directed layer each direction is an independent trial, so there are `n(n−1)` possibilities —
twice as many. This is why directedness belongs to the *layer* rather than to the network.

**Degree distribution.** Each agent has `n−1` independent chances of a tie, so its degree is
binomial with mean `p(n−1)`. For large `n` and small `p` this is approximately Poisson — which is
the model's best-known limitation, since real social networks have heavy-tailed degree
distributions that Erdős–Rényi cannot produce.

**The giant component.** This is the interesting part. Below

$$ p^{*} = \frac{1}{n-1} $$

the largest connected group has `O(log n)` members. Above it, a constant *fraction* of the entire
population is mutually reachable. The transition is abrupt: at `n = 1000`, a quarter of the
threshold leaves the largest component under 100 people, and four times the threshold puts more
than half the population in one component. There is a test asserting exactly that, because a
generator that is subtly wrong still produces plausible-looking graphs — but it will not reproduce
a sharp phase transition at the right place.

---

## The bug

**The program never used its Erdős–Rényi parameter.** Here is the last line of the method that
computed it:

```java
private static void generarParametroErdos(double p, int m, int n) {
    double m1 = combinatoria(n, 2) * p;
    ...
    probabilidad = Math.pow(p, m1) * (Math.pow(1 - p, (combinatoria(n, 2) - m1)));
    System.out.print("\n\n>>Probabilidad: " + probabilidad + "\n\n");
    probabilidad = 0.5;                          // <- every line above is discarded
}
```

Every network the prototype ever generated used `p = 0.5`. The computed value was printed and then
thrown away one line later.

**And the computation was doubly broken anyway.** The expression `p^m (1−p)^(C(n,2)−m)` is the
probability of one *specific* labelled graph having exactly `m` edges — not a per-tie probability,
and not the ensemble probability either, which would need a binomial coefficient in front. But the
arithmetic settles it before the modelling does. For the prototype's own values, `n = 100` and
`p = 0.9`:

| | |
|---|---|
| possible ties, `C(100,2)` | 4 950 |
| `m = 0.9 × 4950` | 4 455 |
| `log₁₀` of the expression | **−698.85** |
| smallest positive `double` | ≈ `1e−308` |

The expression underflows to **exactly 0.0**. Had the hardcoded `0.5` not been there, the program
would have generated a network with no ties whatsoever — and reported nothing unusual. There is a
test that evaluates the 2015 expression and asserts it comes out zero.

So the two defects concealed each other: the hardcoded value hid the underflow, and the underflow
would have made the hardcoded value look necessary.

### And a third, quieter one

```java
private static long combinatoria(long x, int y) {
    return (x * (x - 1)) / y;
}
```

This ignores `y` except as a divisor. It happens to give the right answer for `y = 2`, which is
the only value it was ever called with, and nonsense for anything else — `combinatoria(5, 3)`
returns 6 where C(5,3) is 10. A method named for the binomial coefficient that computes it only in
one case is a trap laid for the next person. `Layer.possibleTies(int)` replaces it, and says in
its name exactly what it counts.

---

## What else changed

**Only realised ties are represented.** The 2015 code created a `Conexion` object for every
*possible* connection and set an `esActiva` flag — 19 800 objects for 100 agents, three layers.
Memory grew with the search space rather than with the answer, which is why the prototype could
not be run at interesting sizes. Now the generator returns the ties that exist, and `n = 100 000`
is unremarkable.

**Agents are values.** `Agente` had no `equals` or `hashCode`, so two agents with the same name
were different objects. Any `Set<Agente>` would have held duplicates and any degree count keyed on
agents would have been wrong. `Agent` is a record, so identity follows from the id.

**`long` arithmetic in the tie counts.** At 100 000 agents an undirected layer has 4 999 950 000
possible ties. In `int` that silently goes negative. There is a test at exactly that size.

**Isolated agents are counted.** `degrees()` returns an entry for every agent, including those
with none. Omitting them makes the mean degree an average over *connected* agents, which is a
different and much larger number than the average over the population.

**`SplittableRandom`, seeded.** `Math.random()` cannot be seeded, so no 2015 result was
reproducible. It also uses a 48-bit linear congruential generator, which is weak for a program
that is nothing but a long sequence of Bernoulli trials.

---

## Development

```bash
mvn verify          # compiles with -Xlint:all -Werror, runs 22 tests
mvn test
```

Tests assert *properties*: that a seeded run reproduces bit-for-bit, that the realised tie count
falls within three standard deviations of its binomial expectation, that a directed layer's two
arcs are independent trials rather than mirrors, that the giant component appears above the
threshold and not below it, and that the 2015 probability expression underflows to zero.

CI builds on Java 21 and 25, and runs the program afterwards — a jar that builds but does not run
is not evidence of anything.

---

## Provenance

The original NetBeans project is preserved in [`docs/netbeans-2015/`](docs/netbeans-2015/),
transcoded from Latin-1 to UTF-8, and the pre-rewrite state is one command away:

```bash
git checkout prototype-2015
```

| 2015 | Now | Why |
|---|---|---|
| `probabilidad = 0.5;` at the end of the parameter method | the parameter is used | **every network was generated at 0.5**; the computation was dead |
| `p^m (1-p)^(C-m)` as a per-tie probability | `G(n, p)` as defined | it underflows to exactly 0 at the prototype's own values |
| `combinatoria(x, y)` ignoring `y` | `Layer.possibleTies(int)` | correct only for `y = 2`, and named as if general |
| a `Conexion` per *possible* tie | only realised ties | memory grew with the search space, not the answer |
| `Agente` with no `equals`/`hashCode` | `record Agent` | two agents with one name were two objects |
| raw `ArrayList`, `Object` accessors, casts at every use | generics | the compiler could not catch a wrong type |
| `int` tie counts | `long` | overflows silently past ~65 000 agents |
| `Math.random()` | seeded `SplittableRandom` | no 2015 result could be reproduced |
| hardcoded 100 agents, 3 layers | command-line arguments | one network could be studied |
| Ant, NetBeans project files, Java 7 | Maven, Java 21 | it built on one machine |
| unused `Toolkit`, `BigInteger`, dead `factorial` overflowing past 20 | removed | noise, and a trap |
| Latin-1 sources | UTF-8 | mojibake in any modern editor |
| no tests | 22 tests against analytic values | none of the three defects above was visible by reading |

Original prototype: John Edisson Tapias Zarrazola, Universidad de Antioquia, Medellín. Commits
dated 28–29 April 2015.

> **A note on attribution.** The 2015 commits carry two different git identities,
> `jedisson.tapias` and `galileo`. They are the same person: `galileo` was the author's Linux
> account. The prototype is sole-authored.

## Where to go next

- Erdős & Rényi (1959), *On random graphs I* — six pages, and the origin of everything above.
- Newman, *Networks: An Introduction*, ch. 12 — the model's properties derived at length,
  including the giant-component threshold.
- Kivelä et al. (2014), *Multilayer networks*, Journal of Complex Networks 2(3) — the formal
  framework for what this prototype was reaching toward.
- Barabási & Albert (1999), *Emergence of scaling in random networks* — why real social networks
  are **not** Erdős–Rényi, which is the most useful thing to know about Erdős–Rényi.

## Citation

See [`CITATION.cff`](CITATION.cff), or:

> Tapias Zarrazola, J. E. *multilayer-erdos-renyi: Erdős–Rényi random graphs over a
> multilayer social network.* Version 1.0.0, 2026.
> https://github.com/Kemquiros/Erdos-Renyi-social-Network-prototype

## License

MIT — see [`LICENSE`](LICENSE).
