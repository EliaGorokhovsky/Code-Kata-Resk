
package com.fairviewcodeclub.resk.logic

/**
 * The stage for global conflict!
 */
class World(val size: Int, val colors: Array<ReskColor>) {

    val nodes = Array(this.size * this.size) { Node(it) }
    val connections = mutableSetOf<Connection>()

    init {
        for (node in this.nodes) {
            arrayOf(
                    node.index - this.size,
                    node.index + this.size,
                    node.index - 1,
                    node.index + 1
            ).filter { 0 <= it && it < this.nodes.size }
                    .forEach {
                        this.connections.add(Connection(node.index, it))
                    }
        }
    }

    fun getAdjacencies(index: Int): List<Int> {
        return this.connections.mapNotNull { it.other(index) }
    }

}
