package com.dayanruben.sssp

import kotlin.test.Test
import kotlin.test.assertTrue

class SSSPTest {

    private fun assertAlmostEquals(expected: Double, actual: Double, eps: Double = 1e-9) {
        if (expected.isInfinite() && actual.isInfinite()) return
        assertTrue(kotlin.math.abs(expected - actual) <= eps, "Expected $expected, got $actual")
    }

    private fun assertArrayAlmostEquals(expected: DoubleArray, actual: DoubleArray, eps: Double = 1e-9) {
        assertTrue(expected.size == actual.size, "Different sizes: expected ${expected.size}, got ${actual.size}")
        for (i in expected.indices) {
            assertAlmostEquals(expected[i], actual[i], eps)
        }
    }

    @Test
    fun chainGraphShortestPaths() {
        // Chain graph: 0 -> 1 (1), 1 -> 2 (2), 2 -> 3 (3)
        val n = 4
        val adj: List<List<Pair<Int, Double>>> = listOf(
            listOf(1 to 1.0),
            listOf(2 to 2.0),
            listOf(3 to 3.0),
            emptyList()
        )
        val s = 0
        val dist = singleSourceShortestPaths(n, adj, s)
        val expected = doubleArrayOf(0.0, 1.0, 3.0, 6.0)
        assertArrayAlmostEquals(expected, dist)
    }

    @Test
    fun disconnectedNodeRemainsInfinite() {
        // Node 4 is disconnected
        val n = 5
        val adj: List<List<Pair<Int, Double>>> = listOf(
            listOf(1 to 2.0),
            listOf(2 to 2.0),
            listOf(3 to 2.0),
            emptyList(),
            emptyList() // node 4 isolated
        )
        val s = 0
        val dist = singleSourceShortestPaths(n, adj, s)
        assertAlmostEquals(0.0, dist[0])
        assertTrue(dist[4].isInfinite(), "Expected INF for disconnected node 4, got ${dist[4]}")
    }

    @Test
    fun starFromSourceShortestPaths() {
        // Star: 0 -> 1 (1), 0 -> 2 (2), 0 -> 3 (3)
        val n = 4
        val adj: List<List<Pair<Int, Double>>> = listOf(
            listOf(1 to 1.0, 2 to 2.0, 3 to 3.0),
            emptyList(),
            emptyList(),
            emptyList()
        )
        val s = 0
        val dist = singleSourceShortestPaths(n, adj, s)
        val expected = doubleArrayOf(0.0, 1.0, 2.0, 3.0)
        assertArrayAlmostEquals(expected, dist)
    }
}
