package com.fairviewcodeclub.resk.logic

/**
 * A country!
 */
class Node(val index: Int) {

    var troops = 0
    var owner: ReskColor? = null

}


class Connection(val a: Int, val b: Int) {

    fun other(index: Int): Int? {
        return if(this.a == index) this.b else if(this.b == index) this.a else null
    }

    override fun equals(other: Any?): Boolean {
        return other is Connection &&
                (other.a == this.b && other.b == this.a
                ||
                other.a == this.a && other.b == this.b)
    }

}