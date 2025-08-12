package com.dayanruben.sssp

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow

/**
 * Single-source shortest paths (SSSP) for directed graphs with non-negative edge weights.
 *
 * This is the public entry point of the library. For small graphs (n less than or equal to
 * [dijkstraFallbackThreshold]) it runs a standard Dijkstra; for larger graphs it uses an
 * optimized BMSSP-based routine under the hood. Helper algorithms and data structures are
 * kept internal to preserve a minimal public surface.
 *
 * Parameters:
 * - n: total number of vertices in the graph. Must be >= 0. Vertices are indexed [0, n).
 * - adj: adjacency list of size n where adj[u] contains pairs (v, w) meaning a directed
 *   edge u -> v with weight w.
 *   Requirements:
 *   - 0 <= v < n for every (v, _ ) in adj[u]
 *   - w >= 0.0 (non-negative weights; negative edges are not supported)
 *   - If a vertex has no outgoing edges, use an empty list for that position.
 * - s: source vertex index. Must satisfy 0 <= s < n.
 * - dijkstraFallbackThreshold: if n <= this value, Dijkstra is used; otherwise the optimized
 *   algorithm is used. Negative values are treated as 0. Default is 64, matching prior behavior.
 *
 * Returns:
 * - DoubleArray of length n where index i holds the length of the shortest path from s to i.
 *   Unreachable vertices will have Double.POSITIVE_INFINITY.
 *
 * Notes/Behavior:
 * - The graph is treated as directed. For undirected graphs, add edges in both directions.
 * - Input validation is not enforced at runtime for performance; invalid indices or sizes may
 *   result in IndexOutOfBounds exceptions.
 *
 * Example:
 * val n = 4
 * val adj = listOf(
 *     listOf(1 to 1.0, 2 to 4.0), // 0 -> 1 (1), 0 -> 2 (4)
 *     listOf(2 to 2.0, 3 to 6.0), // 1 -> 2 (2), 1 -> 3 (6)
 *     listOf(3 to 3.0),           // 2 -> 3 (3)
 *     emptyList()
 * )
 * val s = 0
 * val dist = singleSourceShortestPaths(n, adj, s)
 * // dist = [0.0, 1.0, 3.0, 6.0]
 */
fun singleSourceShortestPaths(
    n: Int,
    adj: List<List<Pair<Int, Double>>>,
    s: Int,
    dijkstraFallbackThreshold: Int = 64,
): DoubleArray {
    // For small graphs, use a straightforward Dijkstra to ensure correctness and avoid
    // edge-case sensitivity of the advanced BMSSP implementation on tiny inputs.
    val threshold = if (dijkstraFallbackThreshold < 0) 0 else dijkstraFallbackThreshold
    if (n <= threshold) return dijkstra(n, adj, s)

    val log2n = ln(n.toDouble()) / ln(2.0)
    val k = floor(log2n.pow(1.0 / 3.0)).toInt()
    val t = floor(log2n.pow(2.0 / 3.0)).toInt()
    val l = ceil(log2n / t.toDouble()).toInt()

    val (newN, newAdj, rep) = transformGraph(n, adj, s)

    val d = DoubleArray(newN) { Double.POSITIVE_INFINITY }
    d[rep[s]] = 0.0
    val pred = IntArray(newN) { -1 }

    BMSSP(l, Double.POSITIVE_INFINITY, setOf(rep[s]), newAdj, d, pred, k, t)

    return DoubleArray(n) { d[rep[it]] }
}
