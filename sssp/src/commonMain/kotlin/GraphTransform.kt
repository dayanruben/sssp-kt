package com.dayanruben.sssp

internal fun transformGraph(n: Int, adj: List<List<Pair<Int, Double>>>, s: Int): Triple<Int, List<MutableList<Pair<Int, Double>>>, IntArray> {
    val inNeighbors = Array(n) { mutableSetOf<Int>() }
    val outNeighbors = Array(n) { mutableSetOf<Int>() }
    for (u in 0 until n) {
        for ((v, _) in adj[u]) {
            outNeighbors[u].add(v)
            inNeighbors[v].add(u)
        }
    }

    val allNeighbors = Array(n) { mutableSetOf<Int>().apply {
        addAll(inNeighbors[it])
        addAll(outNeighbors[it])
    } }

    val nodeId = mutableMapOf<Pair<Int, Int>, Int>()
    var idCounter = 0
    val rep = IntArray(n) { -1 }
    val newAdj = mutableListOf<MutableList<Pair<Int, Double>>>()

    for (v in 0 until n) {
        val neighList = allNeighbors[v].toList()
        if (neighList.isEmpty()) {
            rep[v] = idCounter
            newAdj.add(mutableListOf())
            idCounter++
            continue
        }

        val cycleIds = mutableListOf<Int>()
        for (w in neighList) {
            val pair = Pair(v, w)
            nodeId[pair] = idCounter
            cycleIds.add(idCounter)
            if (rep[v] == -1) rep[v] = idCounter
            newAdj.add(mutableListOf())
            idCounter++
        }

        // Add cycle edges with zero weight
        for (i in 0 until neighList.size - 1) {
            newAdj[cycleIds[i]].add(Pair(cycleIds[i + 1], 0.0))
        }
        newAdj[cycleIds.last()].add(Pair(cycleIds[0], 0.0))
    }

    // Add original edges
    for (u in 0 until n) {
        for ((v, wuv) in adj[u]) {
            val xuv = nodeId[Pair(u, v)]!!
            val xvu = nodeId[Pair(v, u)]!!
            newAdj[xuv].add(Pair(xvu, wuv))
        }
    }

    return Triple(idCounter, newAdj, rep)
}
