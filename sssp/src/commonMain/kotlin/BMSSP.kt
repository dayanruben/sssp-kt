package com.dayanruben.sssp

import kotlin.math.min

internal fun BMSSP(l: Int, B: Double, S: Set<Int>, adj: List<MutableList<Pair<Int, Double>>>, d: DoubleArray, pred: IntArray, k: Int, t: Int): Pair<Double, Set<Int>> {
    if (l == 0) {
        return baseCase(B, S, adj, d, pred, k)
    }

    val (P, W) = findPivots(B, S, adj, d, pred, k)

    val M = 1 shl ((l - 1) * t)
    val partialHeap = PartialHeap(M, B)
    P.forEach { partialHeap.insert(it, d[it]) }

    var i = 0
    var bPrime0 = if (P.isEmpty()) B else P.minOf { d[it] }
    val U = mutableSetOf<Int>()

    var lastBPrime = bPrime0

    while (U.size < k * (1 shl (l * t)) && !partialHeap.isEmpty()) {
        i++
        val (bi, si) = partialHeap.pull()
        val (bPrimeNew, ui) = BMSSP(l - 1, bi, si, adj, d, pred, k, t)
        U.addAll(ui)
        val kSet = mutableSetOf<Pair<Int, Double>>()
        for (u in ui) {
            for ((v, wuv) in adj[u]) {
                if (d[u] + wuv <= d[v]) {
                    d[v] = d[u] + wuv
                    pred[v] = u
                    val newVal = d[u] + wuv
                    if (newVal >= bi && newVal < B) {
                        partialHeap.insert(v, newVal)
                    } else if (newVal >= bPrimeNew && newVal < bi) {
                        kSet.add(Pair(v, newVal))
                    }
                }
            }
        }
        val addBack = si.filter { d[it] >= bPrimeNew && d[it] < bi }.map { Pair(it, d[it]) }.toSet()
        partialHeap.batchPrepend(kSet + addBack)
        lastBPrime = bPrimeNew
    }

    val bPrime = min(lastBPrime, B)
    val addW = W.filter { d[it] < bPrime }.toSet()
    U.addAll(addW)
    return Pair(bPrime, U)
}

internal fun baseCase(B: Double, S: Set<Int>, adj: List<MutableList<Pair<Int, Double>>>, d: DoubleArray, pred: IntArray, k: Int): Pair<Double, Set<Int>> {
    val x = S.first()
    val u0 = mutableSetOf(x)
    val heap = BinaryHeap<Pair<Int, Double>>(compareBy { it.second })
    heap.add(Pair(x, d[x]))

    while (heap.isNotEmpty() && u0.size < k + 1) {
        val (u, dist) = heap.poll()!!
        if (dist > d[u]) continue
        u0.add(u)
        for ((v, wuv) in adj[u]) {
            if (d[u] + wuv < d[v] && d[u] + wuv < B) {
                d[v] = d[u] + wuv
                pred[v] = u
                heap.add(Pair(v, d[v]))
            }
        }
    }

    return if (u0.size <= k) {
        Pair(B, u0)
    } else {
        val maxD = u0.maxOf { d[it] }
        val u = u0.filter { d[it] < maxD }.toSet()
        Pair(maxD, u)
    }
}

internal fun findPivots(B: Double, S: Set<Int>, adj: List<MutableList<Pair<Int, Double>>>, d: DoubleArray, pred: IntArray, k: Int): Pair<Set<Int>, Set<Int>> {
    val w = mutableSetOf<Int>().apply { addAll(S) }
    var wi = S.toMutableSet()
    for (i in 1..k) {
        val newWi = mutableSetOf<Int>()
        for (u in wi) {
            for ((v, wuv) in adj[u]) {
                if (d[u] + wuv <= d[v]) {
                    d[v] = d[u] + wuv
                    pred[v] = u
                    if (d[u] + wuv < B) {
                        newWi.add(v)
                    }
                }
            }
        }
        w.addAll(newWi)
        if (w.size > k * S.size) {
            return Pair(S, w)
        }
        wi = newWi
    }

    val f = mutableListOf<Pair<Int, Int>>()
    for (u in w) {
        for ((v, wuv) in adj[u]) {
            if (d[v] == d[u] + wuv) {
                f.add(Pair(u, v))
            }
        }
    }

    val treeAdj = Array(adj.size) { mutableListOf<Int>() }
    f.forEach { treeAdj[it.first].add(it.second) }

    val sizes = IntArray(adj.size)
    fun dfs(u: Int): Int {
        if (sizes[u] > 0) return sizes[u]
        sizes[u] = 1
        treeAdj[u].forEach { sizes[u] += dfs(it) }
        return sizes[u]
    }

    val p = mutableSetOf<Int>()
    S.forEach { if (dfs(it) >= k) p.add(it) }
    return Pair(p, w)
}
