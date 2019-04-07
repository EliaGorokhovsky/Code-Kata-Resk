package com.fairviewcodeclub.resk.logic

/**
 * The object in which the game is played
 * A world is a mathematical graph of nodes and edges
 * World constructor takes in sidelength and the colors of the playing players
 * All methods are synchronized to prevent threading issues
 */
class World(val size: Int, colors: Array<ReskColor>) {

	var players = colors
		@Synchronized get() = field
		private set(value) { field = value }
	//The nodes or tiles of the world
    val nodes = Array(this.size * this.size) { Node(it) }
		@Synchronized get() = field
	//The paths between nodes: which tiles connect to which other tiles
    val connections = this.nodes
			.flatMap { node ->
				listOf(node.id + 1, node.id - 1, node.id + this.size, node.id - this.size)
						.filter { 0 <= it && it < this.nodes.size * this.nodes.size }
						.map { Connection(it, node.id) }
			}.toMutableSet()
		@Synchronized get() = field
	//A map of player colors to the amount of card cash they have; each player starts off with 6 default
	val cardCashValues = colors.map { it to 6 }.toMap().toMutableMap()
		@Synchronized get() = field

	//How many turns this world has existed
	var turnCount = 0
		@Synchronized get() = field
		private set(value) { field = value }
	//The team whose turn it is right now
	var currentActor = colors[0]
		@Synchronized get() = field
		private set(value) { field = value }
	//How many troops the current actor still has to commit
	var numberOfTroopsToCommit = 25
		@Synchronized get() = field
		private set(value) { field = value }
	//What troop orders will execute at the end of the current turn
	val troopOrders = mutableListOf<TroopOrder>()
		@Synchronized get() = field

	/**
	 * Commits the given amount of troops to the given location
	 * Troops can only be committed to owned tiles or a corner of the map if no tiles are owned
	 * Player's turn is over once all troops are committed
	 * This method handles managing turn count and current player
	 * Returns method success
	 */
	@Synchronized fun commitNewTroops(locationId: Int, amount: Int): Boolean {
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
				this.nodes[troopOrder.toId].addTroops(Troops(this.currentActor, troopOrder.amount))
				if (this.nodes[troopOrder.toId].troops?.owner == this.currentActor) {
					if (troopOrder.fromId != -1) {
						this.nodes[troopOrder.fromId].addTroops(Troops(this.currentActor, -troopOrder.amount))
					}
					if (previousTileOwner != this.currentActor) {
						val previousCardCash = this.cardCashValues[this.currentActor]!!
						this.cardCashValues[this.currentActor] = previousCardCash + 1
					}
				}
			}
			this.troopOrders.clear()
			this.nodes.filter { it.troops != null && it.troops!!.amount <= 0 }.forEach { it.troops = null }
			fun incrementCurrentActor() {
				this.currentActor = this.players[(this.players.indexOf(this.currentActor) + 1) % this.players.size]
			}
			incrementCurrentActor()
			if (this.currentActor == this.players[0]) {
				this.turnCount++
			}
			if (this.turnCount == 0) {
				this.numberOfTroopsToCommit = 25
			} else {
				val eliminatedPlayers = this.players.filter { this.territoriesOwnedBy(it).isEmpty() }
				while (eliminatedPlayers.contains(this.currentActor)) {
					incrementCurrentActor()
				}
				this.players = this.players.filter { !eliminatedPlayers.contains(it) }.toTypedArray()
				this.numberOfTroopsToCommit = this.territoriesOwnedBy(this.currentActor).size
			}
		}
		return true
	}

	/**
	 * Adds a TroopOrder to move the given amount of troops from the fromId tile to the toId tile
	 * Troops can only be moved to adjacent tiles or to other owned tiles
	 * Returns success of queueing move order
	 */
	@Synchronized fun queueTroopsMove(fromId: Int, toId: Int, amount: Int): Boolean {
		if (this.nodes[fromId].troops?.owner != this.currentActor) {
			return false
		}
		if (this.nodes[fromId].troops!!.amount - this.troopOrders.filter { it.fromId == fromId }.sumBy { it.amount } < amount) {
			return false
		}
		this.troopOrders.add(TroopOrder(fromId, toId, amount))
		return true
	}

	/**
	 * Gets the IDs of the territories owned by the given actor
	 */
	@Synchronized fun territoriesOwnedBy(actor: ReskColor): List<Int> {
		return this.nodes.filter { it.troops?.owner == actor }.map { it.id }
	}

	/**
	 * Gets a list of nodes adjacent to the given node
	 * Nodes are given and returned as indices
	 */
	@Synchronized fun getAdjacencies(tileId: Int): List<Int> {
        return this.connections.mapNotNull { it.connects(tileId) }.distinct()
    }

	/**
	 * Uses a single card cash from the given actor to connect the two tiles given by their IDs
	 * Teams must own both tiles they are connecting
	 * Returns the success of using up a card to do the connection action
	 */
	@Synchronized fun cardConnect(tileId1: Int, tileId2: Int): Boolean {
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

	/**
	 * Uses two card cash to inspire an insurgency of 10 unowned troops on any given tile
	 * If an insurgency is inspired on an owned tile, it would be as if the tile was under attack by 10 unowned troops
	 * Returns success of using card cash to inspire insurgency
	 */
	@Synchronized fun cardInspireInsurgency(tileId: Int): Boolean {
		val cardCashAmount = this.cardCashValues[this.currentActor]!!
		if (cardCashAmount < 2) {
			return false
		}
		this.cardCashValues[this.currentActor] = cardCashAmount - 2
		this.nodes[tileId].addTroops(Troops(null, 10))
		return true
	}

	/**
	 * Uses four card cash to remove all connections that connect the tile of the given ID
	 * Returns success of using card cash to disconnect tile
	 */
	@Synchronized fun cardDisconnect(tileId: Int): Boolean {
		val cardCashAmount = this.cardCashValues[this.currentActor]!!
		if (cardCashAmount < 4 || this.getAdjacencies(tileId).isEmpty()) {
			return false
		}
		this.cardCashValues[this.currentActor] = cardCashAmount - 4
		this.connections.removeIf { it.connects(tileId) != null }
		return true
	}

}
