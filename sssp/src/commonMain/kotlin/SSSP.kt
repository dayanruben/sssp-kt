package com.dayanruben.sssp

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow

/**
 * Public API: Single-Source Shortest Paths.
 * Exposes only the main algorithm entry point, keeping helpers internal.
 *
 * @param dijkstraFallbackThreshold If n <= this threshold, the algorithm falls back to Dijkstra.
 * Defaults to 64 to retain previous behavior.
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
