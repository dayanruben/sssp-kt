package com.dayanruben.sssp

internal class BinaryHeap<T>(private val comparator: Comparator<T>) {
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
