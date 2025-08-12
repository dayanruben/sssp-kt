package com.dayanruben.sssp

internal fun dijkstra(n: Int, adj: List<List<Pair<Int, Double>>>, s: Int): DoubleArray {
    val dist = DoubleArray(n) { Double.POSITIVE_INFINITY }
    dist[s] = 0.0
    val heap = BinaryHeap<Pair<Int, Double>>(compareBy { it.second })
    heap.add(Pair(s, 0.0))
    while (heap.isNotEmpty()) {
        val (u, du) = heap.poll() ?: break
        if (du > dist[u]) continue
        for ((v, wuv) in adj[u]) {
            val nd = du + wuv
            if (nd < dist[v]) {
                dist[v] = nd
                heap.add(Pair(v, nd))
            }
        }
    }
    return dist
}
