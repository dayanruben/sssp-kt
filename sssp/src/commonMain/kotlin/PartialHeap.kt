package com.dayanruben.sssp

internal class PartialHeap(val M: Int, val B: Double) {
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
