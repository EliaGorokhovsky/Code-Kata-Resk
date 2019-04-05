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
				listOf(node.id + 1, node.id - 1, node.id + this.size, node.id - this.size)
						.filter { 0 <= it && it < this.nodes.size * this.nodes.size }
						.map { Connection(it, node.id) }
			}.toMutableSet()
	//A map of player colors to the amount of card cash they have; each player starts off with 6 default
	val cardCashValues = this.colors.map { it to 6 }.toMap().toMutableMap()

	//The team whose turn it is right now
	val currentActor = this.colors[0]
	//How many troops the current actor still has to commit
	var numberOfTroopsToCommit = 25

	/**
	 * Gets the amount of territories owned by the given actor
	 */
	fun amountOfTerritoriesFor(actor: ReskColor): Int {
		return this.nodes.count { it.troops?.owner == actor }
	}

	/**
	 * Gets a list of nodes adjacent to the given node
	 * Nodes are given and returned as indices
	 */
    fun getAdjacencies(tileId: Int): List<Int> {
        return this.connections.mapNotNull { it.connects(tileId) }.distinct()
    }

	/**
	 * Uses a single card cash from the given actor to connect the two tiles given by their IDs
	 * Teams must own both tiles they are connecting
	 * Returns the success of using up a card to do the connection action
	 */
	fun cardConnect(tileId1: Int, tileId2: Int): Boolean {
		val cardCashAmount = this.cardCashValues[this.currentActor]!!
		val tileOwner1 = this.nodes[tileId1].troops?.owner
		val tileOwner2 = this.nodes[tileId2].troops?.owner
		if (cardCashAmount < 1 || this.getAdjacencies(tileId1).contains(tileId2) || tileOwner1 != tileOwner2 || this.currentActor != tileOwner1) {
			return false
		}
		this.cardCashValues[this.currentActor] = cardCashAmount - 1
		this.connections.add(Connection(tileId1, tileId2))
		return true
	}

}
