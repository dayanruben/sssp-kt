# SSSP.kt — Single‑Source Shortest Paths for Kotlin Multiplatform

[![Kotlin](https://img.shields.io/badge/dynamic/toml?url=https%3A%2F%2Fraw.githubusercontent.com%2Fdayanruben%2Fsssp-kt%2Frefs%2Fheads%2Fmain%2Fgradle%2Flibs.versions.toml&query=versions.kotlin&prefix=v&logo=kotlin&label=Kotlin)](./gradle/libs.versions.toml)
[![Gradle](https://img.shields.io/badge/Gradle-9.2.1-blue?logo=gradle)](https://gradle.org)
[![Version](https://img.shields.io/maven-central/v/com.dayanruben/sssp)][mavenCentral]
[![License](https://img.shields.io/github/license/dayanruben/sssp-kt)][license]

A fast, lightweight Kotlin Multiplatform library to compute single‑source shortest path (SSSP) distances on directed graphs with non‑negative edge weights.

- Smart algorithm selection: automatically uses classic Dijkstra for small graphs and a cache‑efficient BMSSP‑based routine for large graphs.
- Tiny public API, no external dependencies.
- Works on Kotlin/JVM, Android, iOS, and Linux (KMP).


## Install

Artifacts are published with the following coordinates:

- Group: `com.dayanruben`
- Artifact: `sssp`
- Version: `0.3.0`

Gradle (Kotlin DSL):

```kotlin
// settings.gradle.kts
// Ensure you have Maven Central in repositories
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

// build.gradle.kts (root or module)
dependencies {
    implementation("com.dayanruben:sssp:0.3.0")
}
```

Gradle (Groovy DSL):

```groovy
// settings.gradle
pluginManagement {
    repositories { mavenCentral() }
}
dependencyResolutionManagement {
    repositories { mavenCentral() }
}

// build.gradle
dependencies {
    implementation "com.dayanruben:sssp:0.3.0"
}
```


## Quick start

Compute distances from a single source s to all vertices 0..n-1. Unreachable vertices get `Double.POSITIVE_INFINITY`.

```kotlin
fun main() {
    val n = 4
    val adj: List<List<Pair<Int, Double>>> = listOf(
        listOf(1 to 1.0, 2 to 4.0), // 0 -> 1 (1), 0 -> 2 (4)
        listOf(2 to 2.0, 3 to 6.0), // 1 -> 2 (2), 1 -> 3 (6)
        listOf(3 to 3.0),           // 2 -> 3 (3)
        emptyList()                 // 3 has no outgoing edges
    )
    val s = 0

    val dist: DoubleArray = singleSourceShortestPaths(n, adj, s)
    println(dist.joinToString()) // 0.0, 1.0, 3.0, 6.0
}
```

Undirected graphs: add edges both ways (u->v and v->u).


## API

Public entry point:

```kotlin
fun singleSourceShortestPaths(
    n: Int,
    adj: List<List<Pair<Int, Double>>>,
    s: Int,
    dijkstraFallbackThreshold: Int = 64,
): DoubleArray
```

Parameters
- n: number of vertices (>= 0). Vertices are 0..n-1.
- adj: adjacency list; for each u, adj[u] is a list of (v, w) meaning an edge u -> v with weight w.
  - v must be in 0 until n
  - w must be >= 0.0 (negative weights are not supported)
  - use `emptyList()` if a vertex has no outgoing edges
- s: source vertex index (0 <= s < n)
- dijkstraFallbackThreshold: if `n <= threshold`, a straightforward Dijkstra is used; otherwise a BMSSP‑based routine is applied. Negative values are treated as 0.

Return value
- DoubleArray of size n with distances from s. Unreachable vertices are `Double.POSITIVE_INFINITY`.

Performance notes
- Dijkstra path: binary heap, suitable for small/medium sparse graphs.
- BMSSP path: uses a graph transformation plus a partial heap to improve cache‑locality and batching on larger graphs. The switch is controlled by `dijkstraFallbackThreshold`.


## Examples

- Chain graph 0→1(1), 1→2(2), 2→3(3): distances are [0, 1, 3, 6].
- Star from source 0→{1,2,3}: distances are [0, 1, 2, 3].
- Disconnected vertex: distance is `INF`.

You can see runnable examples in tests under `sssp/src/commonTest/kotlin/SSSPTest.kt`.


## Supported platforms

This is a Kotlin Multiplatform library with targets:
- JVM
- Android (library variant publishing enabled)
- iOS (x64, arm64, simulator arm64)
- Linux x64


## FAQ

- Does it work with negative weights? No. Only non‑negative edge weights are supported.
- Are graphs directed? Yes. For undirected graphs, add edges in both directions.
- Can I get predecessors/paths? The library computes predecessors internally to update distances, but it currently exposes only distances. Path reconstruction helpers may be added in future versions.
- What about multiple sources? You can add a super‑source connected to all desired sources with 0‑weight edges and run once from the super‑source.


## Versioning

This library follows semantic versioning as much as feasible for a small algorithmic library. See the Releases page for changes.


## Contributing

Issues and pull requests are welcome! Please include:
- a clear description of the problem or enhancement
- if it’s a bug, a minimal reproducible example or a failing test
- performance considerations for algorithmic changes


## References

- The BMSSP-inspired implementation and cache-efficient design draw inspiration from the following paper: https://arxiv.org/pdf/2504.17033

## License

Apache License 2.0 — see [LICENSE](LICENSE).

[mavenCentral]: https://search.maven.org/artifact/com.dayanruben/sssp
[license]: LICENSE