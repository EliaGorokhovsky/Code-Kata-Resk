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
	//A map of player colors to the amount of card cash they have; each player starts off with 6 default
	val cardCashValues = this.colors.map { it to 6 }.toMap()

	/**
	 * Gets a list of nodes adjacent to the given node
	 * Nodes are given and returned as indices
	 */
    fun getAdjacencies(index: Int): List<Int> {
        return this.connections.mapNotNull { it.connects(index) }.distinct()
    }

	/**
	 * Gets the map as a JSON string
	 */
	@Deprecated("Method call is too expensive to be used as a common bread and butter API call")
	override fun toString() : String {
		return "[${this.nodes.joinToString(",") { node -> "{number:${node.index},connectedTo:[${this.getAdjacencies(node.index).joinToString(",")}]}" }}}]"
	}

}
