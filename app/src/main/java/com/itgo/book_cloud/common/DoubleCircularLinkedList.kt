package com.itgo.book_cloud.common

class DoubleCircularLinkedMap<T, R>(private val list: MutableList<MutablePair<T, R>>) {
    var currentItemIdx = 0

    fun move2NextItem(): MutablePair<T, R> {
        currentItemIdx = (currentItemIdx + 1) % list.size
        return getCurrentItem()
    }

    fun move2PreItem(): MutablePair<T, R> {
        currentItemIdx = (currentItemIdx + list.size - 1) % list.size

        return getCurrentItem()
    }

    fun getNextItem(): MutablePair<T, R> {
        val nextItemIdx = (currentItemIdx + 1) % list.size
        return list[nextItemIdx]
    }

    fun getPreItem(): MutablePair<T, R> {
        val preItemIdx = (currentItemIdx + list.size - 1) % list.size
        return list[preItemIdx]
    }

    fun getCurrentItem(): MutablePair<T, R> {
        return list[currentItemIdx]
    }

    fun setNextItemFirst(newV: T) {
        val nextItemIdx = (currentItemIdx + 1) % list.size
        list[nextItemIdx].first = newV
    }

    fun setPreItemFirst(newV: T) {
        val preItemIdx = (currentItemIdx + list.size - 1) % list.size
        list[preItemIdx].first = newV
    }

    fun setCurrentItemFirst(newV: T) {
        list[currentItemIdx].first = newV
    }

    companion object {
        fun <T, R> from(list: MutableList<MutablePair<T, R>>): DoubleCircularLinkedMap<T, R> {
            return DoubleCircularLinkedMap(list)
        }
    }
}