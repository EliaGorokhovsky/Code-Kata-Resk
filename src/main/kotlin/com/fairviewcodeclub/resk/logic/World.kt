package com.fairviewcodeclub.resk.logic

/**
 * The object in which the game is played
 * A world is a mathematical graph of nodes and edges
 * World constructor takes in sidelength and the colors of the playing players
 */
class World(val size: Int, val colors: Array<ReskColor>) {

	//The nodes or tiles of the world
    val nodes = Array(this.size * this.size) { Node(it) }
	//The paths between nodes: which tiles connect to which other tiles
    val connections = this.nodes
			.flatMap { node ->
				listOf(node.index + 1, node.index - 1, node.index + this.size, node.index - this.size)
						.filter { 0 <= it && it < this.nodes.size * this.nodes.size }
						.map { Connection(it, node.index) }
			}.toMutableSet()

	/**
	 * Gets a list of nodes adjacent to the given node
	 * Nodes are given and returned as indices
	 */
    fun getAdjacencies(index: Int): List<Int> {
        return this.connections.mapNotNull { it.connects(index) }
    }

}
