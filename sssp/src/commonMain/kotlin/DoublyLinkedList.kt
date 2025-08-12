package com.dayanruben.sssp

internal data class Node<T>(val value: T, var prev: Node<T>?, var next: Node<T>?) {
    var blockIndex: Int = -1
    var isD0: Boolean = false
}

internal class DoublyLinkedList<T> {
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
