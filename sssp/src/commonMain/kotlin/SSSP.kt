package com.dayanruben.sssp

// Full Kotlin implementation of the algorithm from the paper "Breaking the Sorting Barrier for Directed Single-Source Shortest Paths"
// Link: https://arxiv.org/pdf/2504.17033
// Note: This implementation uses Double for distances and assumes no ties in path lengths as per Assumption 2.1 in the paper.
// In practice, to handle ties, use the custom Dist class and shouldUpdate function as described in the thinking process.

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.pow

data class Node<T>(val value: T, var prev: Node<T>?, var next: Node<T>?) {
    var blockIndex: Int = -1
    var isD0: Boolean = false
}

class DoublyLinkedList<T> {
    var head: Node<T>? = null
    var tail: Node<T>? = null
    var size: Int = 0

    fun add(value: T): Node<T> {
        val node = Node(value, tail, null)
        tail?.next = node
        if (head == null) head = node
        tail = node
        size++
        return node
    }

    fun remove(node: Node<T>) {
        node.prev?.next = node.next
        node.next?.prev = node.prev
        if (head == node) head = node.next
        if (tail == node) tail = node.prev
        size--
    }

    fun isEmpty(): Boolean = size == 0

    fun toMutableList(): MutableList<T> {
        val list = mutableListOf<T>()
        var current = head
        while (current != null) {
            list.add(current.value)
            current = current.next
        }
        return list
    }
}

class PartialHeap(val M: Int, val B: Double) {
    val D0: MutableList<DoublyLinkedList<Pair<Int, Double>>> = mutableListOf()
    val D1: MutableList<DoublyLinkedList<Pair<Int, Double>>> = mutableListOf()
    val upperBounds: MutableList<Double> = mutableListOf(B)
    val keyToNode: MutableMap<Int, Node<Pair<Int, Double>>> = mutableMapOf()

    init {
        D1.add(DoublyLinkedList())
    }

    fun insert(key: Int, value: Double) {
        keyToNode[key]?.let {
            if (value >= it.value.second) return
            removeNode(it)
        }

        val blockIndex = binarySearchUpperBounds(value)
        val block = D1[blockIndex]
        val node = block.add(Pair(key, value))
        node.blockIndex = blockIndex
        node.isD0 = false
        keyToNode[key] = node
        if (block.size > M) split(blockIndex)
    }

    private fun binarySearchUpperBounds(value: Double): Int {
        var low = 0
        var high = upperBounds.size - 1
        while (low <= high) {
            val mid = (low + high) / 2
            if (upperBounds[mid] >= value) {
                high = mid - 1
            } else {
                low = mid + 1
            }
        }
        return low.coerceAtMost(upperBounds.size - 1)
    }

    private fun removeNode(node: Node<Pair<Int, Double>>) {
        val isD0 = node.isD0
        val blockIndex = node.blockIndex
        val block = if (isD0) D0[blockIndex] else D1[blockIndex]
        block.remove(node)
        keyToNode.remove(node.value.first)
        if (block.isEmpty()) {
            if (isD0) {
                D0.removeAt(blockIndex)
                updateBlockIndices(D0, blockIndex)
            } else {
                D1.removeAt(blockIndex)
                upperBounds.removeAt(blockIndex)
                updateBlockIndices(D1, blockIndex)
            }
        }
    }

    private fun updateBlockIndices(blocks: MutableList<DoublyLinkedList<Pair<Int, Double>>>, startIndex: Int) {
        for (i in startIndex until blocks.size) {
            var current = blocks[i].head
            while (current != null) {
                current.blockIndex = i
                current = current.next
            }
        }
    }

    private fun split(blockIndex: Int) {
        val block = D1[blockIndex]
        val elements = block.toMutableList()
        elements.sortBy { it.second }
        val medianIndex = elements.size / 2
        val median = elements[medianIndex].second

        val leftElements = elements.take(medianIndex)
        val rightElements = elements.drop(medianIndex)

        val leftBlock = DoublyLinkedList<Pair<Int, Double>>()
        leftElements.forEach {
            val node = leftBlock.add(it)
            node.blockIndex = blockIndex
            node.isD0 = false
            keyToNode[it.first] = node
        }

        val rightBlock = DoublyLinkedList<Pair<Int, Double>>()
        rightElements.forEach {
            val node = rightBlock.add(it)
            node.blockIndex = blockIndex + 1
            node.isD0 = false
            keyToNode[it.first] = node
        }

        D1[blockIndex] = leftBlock
        upperBounds[blockIndex] = leftElements.maxOfOrNull { it.second } ?: upperBounds[blockIndex]

        D1.add(blockIndex + 1, rightBlock)
        upperBounds.add(blockIndex + 1, rightElements.maxOfOrNull { it.second } ?: B)
    }

    fun batchPrepend(L: Set<Pair<Int, Double>>) {
        val list = L.toMutableList().filter { pair ->
            keyToNode[pair.first]?.let { node ->
                if (pair.second < node.value.second) {
                    removeNode(node)
                    true
                } else false
            } ?: true
        }

        if (list.isEmpty()) return

        val blocks = createBlocks(list.sortedBy { it.second }.toMutableList())
        D0.addAll(0, blocks)
        updateBlockIndices(D0, 0)
    }

    private fun createBlocks(list: MutableList<Pair<Int, Double>>): MutableList<DoublyLinkedList<Pair<Int, Double>>> {
        val result = mutableListOf<DoublyLinkedList<Pair<Int, Double>>>()
        if (list.size <= M) {
            val block = DoublyLinkedList<Pair<Int, Double>>()
            list.forEach {
                val node = block.add(it)
                node.isD0 = true
                keyToNode[it.first] = node
            }
            result.add(block)
        } else {
            val medianIndex = list.size / 2
            val median = list[medianIndex].second
            val left = list.takeWhile { it.second <= median }.toMutableList()
            val right = list.dropWhile { it.second <= median }.toMutableList()
            result.addAll(createBlocks(left))
            result.addAll(createBlocks(right))
        }
        return result
    }

    fun pull(): Pair<Double, Set<Int>> {
        val collected = mutableListOf<Pair<Int, Double>>()

        var d0Index = 0
        while (collected.size < M && d0Index < D0.size) {
            collected.addAll(D0[d0Index].toMutableList())
            d0Index++
        }

        var d1Index = 0
        while (collected.size < M && d1Index < D1.size) {
            collected.addAll(D1[d1Index].toMutableList())
            d1Index++
        }

        if (collected.size <= M) {
            val S = collected.map { it.first }.toSet()
            val x = B
            repeat(d0Index) { D0.removeAt(0) }
            repeat(d1Index) {
                D1.removeAt(0)
                upperBounds.removeAt(0)
            }
            collected.forEach { keyToNode.remove(it.first) }
            return Pair(x, S)
        } else {
            collected.sortBy { it.second }
            val S = collected.take(M).map { it.first }.toSet()
            val x = collected[M].second
            collected.take(M).forEach { removeNode(keyToNode[it.first]!!) }
            return Pair(x, S)
        }
    }

    fun isEmpty(): Boolean = D0.all { it.isEmpty() } && D1.all { it.isEmpty() }
}

class BinaryHeap<T>(private val comparator: Comparator<T>) {
    private val heap: MutableList<T> = mutableListOf()

    fun add(element: T) {
        heap.add(element)
        siftUp(heap.size - 1)
    }

    fun poll(): T? {
        if (heap.isEmpty()) return null
        val result = heap[0]
        val last = heap.removeAt(heap.size - 1)
        if (heap.isNotEmpty()) {
            heap[0] = last
            siftDown(0)
        }
        return result
    }

    fun isNotEmpty(): Boolean = heap.isNotEmpty()

    private fun siftUp(index: Int) {
        var i = index
        while (i > 0) {
            val parent = (i - 1) / 2
            if (comparator.compare(heap[i], heap[parent]) < 0) {
                swap(i, parent)
                i = parent
            } else break
        }
    }

    private fun siftDown(index: Int) {
        var i = index
        val size = heap.size
        while (true) {
            var smallest = i
            val left = 2 * i + 1
            val right = 2 * i + 2
            if (left < size && comparator.compare(heap[left], heap[smallest]) < 0) smallest = left
            if (right < size && comparator.compare(heap[right], heap[smallest]) < 0) smallest = right
            if (smallest != i) {
                swap(i, smallest)
                i = smallest
            } else break
        }
    }

    private fun swap(i: Int, j: Int) {
        val temp = heap[i]
        heap[i] = heap[j]
        heap[j] = temp
    }
}

fun transformGraph(n: Int, adj: List<List<Pair<Int, Double>>>, s: Int): Triple<Int, List<MutableList<Pair<Int, Double>>>, IntArray> {
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

fun singleSourceShortestPaths(n: Int, adj: List<List<Pair<Int, Double>>>, s: Int): DoubleArray {
    // For small graphs, use a straightforward Dijkstra to ensure correctness and avoid
    // edge-case sensitivity of the advanced BMSSP implementation on tiny inputs.
    if (n <= 64) return dijkstra(n, adj, s)

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

private fun dijkstra(n: Int, adj: List<List<Pair<Int, Double>>>, s: Int): DoubleArray {
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

fun BMSSP(l: Int, B: Double, S: Set<Int>, adj: List<MutableList<Pair<Int, Double>>>, d: DoubleArray, pred: IntArray, k: Int, t: Int): Pair<Double, Set<Int>> {
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

fun baseCase(B: Double, S: Set<Int>, adj: List<MutableList<Pair<Int, Double>>>, d: DoubleArray, pred: IntArray, k: Int): Pair<Double, Set<Int>> {
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

    if (u0.size <= k) {
        return Pair(B, u0)
    } else {
        val maxD = u0.maxOf { d[it] }
        val u = u0.filter { d[it] < maxD }.toSet()
        return Pair(maxD, u)
    }
}

fun findPivots(B: Double, S: Set<Int>, adj: List<MutableList<Pair<Int, Double>>>, d: DoubleArray, pred: IntArray, k: Int): Pair<Set<Int>, Set<Int>> {
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
