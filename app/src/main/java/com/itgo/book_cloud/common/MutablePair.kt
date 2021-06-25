package com.itgo.book_cloud.common

import java.io.Serializable

data class MutablePair<A, B>(
    var first: A,
    var second: B
) : Serializable {

    override fun toString(): String = "($first, $second)"
}

infix fun <A, B> A.mto(that: B): MutablePair<A, B> = MutablePair(this, that)