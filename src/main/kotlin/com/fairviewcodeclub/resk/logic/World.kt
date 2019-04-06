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

	//How many turns this world has existed
	var turnCount = 0
	//The team whose turn it is right now
	var currentActor = this.colors[0]
	//How many troops the current actor still has to commit
	var numberOfTroopsToCommit = 25
	//What troop orders will execute at the end of the current turn
	val troopOrders = mutableListOf<TroopOrder>()

	/**
	 * Commits the given amount of troops to the given location
	 * Troops can only be committed to owned tiles or a corner of the map if no tiles are owned
	 * Player's turn is over once all troops are committed
	 * This method handles managing turn count and current player
	 * Returns method success
	 */
	fun commitNewTroops(locationId: Int, amount: Int): Boolean {
		if (amount > this.numberOfTroopsToCommit || amount == 0) {
			return false
		}
		if (this.nodes[locationId].troops?.owner != this.currentActor || this.territoriesOwnedBy(this.currentActor).isEmpty() && !arrayOf(0, size - 1, size * (size - 1), size * size - 1).contains(locationId)) {
			return false
		}
		this.troopOrders.add(TroopOrder(-1, locationId, amount))
		this.numberOfTroopsToCommit -= amount
		if (this.numberOfTroopsToCommit == 0) {
			this.troopOrders.forEach { troopOrder ->
				val previousTileOwner = this.nodes[troopOrder.toId].troops?.owner
				if (troopOrder.fromId == -1) {
					this.nodes[troopOrder.toId].addTroops(Troops(this.currentActor, troopOrder.amount))
				} else {
					this.nodes[troopOrder.fromId].addTroops(Troops(this.currentActor, -troopOrder.amount))
					this.nodes[troopOrder.toId].addTroops(Troops(this.currentActor, troopOrder.amount))
				}
				if (previousTileOwner != this.currentActor && this.nodes[troopOrder.toId].troops?.owner == this.currentActor) {
					val previousCardCash = this.cardCashValues[this.currentActor]!!
					this.cardCashValues[this.currentActor] = previousCardCash + 1
				}
			}
			this.troopOrders.clear()
			fun incrementCurrentActor() {
				this.currentActor = this.colors[(this.colors.indexOf(this.currentActor) + 1) % this.colors.size]
			}
			incrementCurrentActor()
			if (this.currentActor == this.colors[0]) {
				this.turnCount++
			}
			if (this.turnCount == 0) {
				this.numberOfTroopsToCommit = 25
			} else {
				val eliminatedPlayers = this.colors.filter { this.territoriesOwnedBy(it).isEmpty() }
				while (eliminatedPlayers.contains(this.currentActor)) {
					incrementCurrentActor()
				}
				this.numberOfTroopsToCommit = this.territoriesOwnedBy(this.currentActor).size
			}
		}
		return true
	}

	/**
	 * Gets the IDs of the territories owned by the given actor
	 */
	fun territoriesOwnedBy(actor: ReskColor): List<Int> {
		return this.nodes.filter { it.troops?.owner == actor }.map { it.id }
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
