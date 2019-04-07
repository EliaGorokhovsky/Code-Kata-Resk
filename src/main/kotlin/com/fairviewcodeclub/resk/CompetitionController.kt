package com.fairviewcodeclub.resk

import com.fairviewcodeclub.resk.logic.ReskColor
import com.fairviewcodeclub.resk.logic.World
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * The class that handles managing requests for the competition game
 * Performs validity checks and passes data to World class
 */
@RestController
@RequestMapping(value=["/api"])
class CompetitionController {

	//The world that each player in the competition play in
	val world = World(25, ReskColor.values().toList().shuffled().toTypedArray())
	//The log of actions that were taken
	val actionLog = mutableListOf<String>()

	/**
	 * Returns whether the given tile ID is allowed
	 */
	private fun isTileIdValid(tileId: Int): Boolean {
		return 0 <= tileId && tileId < this.world.size * this.world.size
	}

	/**
	 * Gets the order of the teams
	 */
	@RequestMapping(value=["/teams/order"], method=[RequestMethod.GET])
	fun getPlayerOrder(): String {
		return "[${this.world.players.joinToString(",") { it.name }}]"
	}

	/**
	 * Gets whose turn it is right now
	 */
	@RequestMapping(value=["/teams/current"], method=[RequestMethod.GET])
	fun getCurrentActor(): String {
		return this.world.currentActor.name
	}

	/**
	 * Gets the tile IDs for tiles owned by the given team color
	 * If the given team color doesn't exist, null is returned
	 */
	@RequestMapping(value=["/teams/territories"], method=[RequestMethod.GET])
	fun getTerritoriesFor(@RequestParam teamColor: String): String {
		val color = this.world.players.firstOrNull { it.name == teamColor } ?: return "null"
		return "[${this.world.territoriesOwnedBy(color).joinToString(",")}]"
	}

	/**
	 * Gets the total number of tiles in the world
	 */
	@RequestMapping(value=["/board/size"], method=[RequestMethod.GET])
	fun getBoardSize(): Int {
		return this.world.nodes.size
	}

	/**
	 * Gets the IDs of the tiles that are adjacent to the tile of the given ID
	 * If the given ID is wrong, null is returned
	 */
	@RequestMapping(value=["/board/adjacencies"], method=[RequestMethod.GET])
	fun getAdjacenciesForTile(@RequestParam id: Int): String {
		if (!this.isTileIdValid(id)) {
			return "null"
		}
		return "[${this.world.getAdjacencies(id).joinToString(",")}]"
	}

	/**
	 * Gets the troops on the tile of the given ID
	 * null means there are no troops or the tile ID is wrong
	 */
	@RequestMapping(value=["/board/troops"], method=[RequestMethod.GET])
	fun getTroopsOnTile(@RequestParam id: Int): String {
		if (!this.isTileIdValid(id)) {
			return "null"
		}
		return "${this.world.nodes[id].troops}"
	}

	/**
	 * Gets an ordered list of every action taken in the form 'team action inputs'
	 * Actions are
	 * 	commit <location> <amount>
	 * 	move <from> <to> <amount>
	 * 	connect <tile1> <tile2>
	 * 	insurgency <tile>
	 * 	disconnect <tile>
	 * 	end
	 * The latest action is at the end.
	 */
	@RequestMapping(value=["/actions"], method=[RequestMethod.GET])
	fun getActionLog(): String {
		return "[${this.actionLog.joinToString(",")}]"
	}

	/**
	 * Allows the team of the given password to commit troops to an owned tile if it is their turn
	 * Returns null if the password is wrong or the team isn't allowed to commit troops yet or the tile ID is wrong or the amount is <= 0
	 * Returns success of adding new troops
	 * Troops don't get added immediately: action of adding troops is queued until the end of the turn
	 * Turn ends once a team commits all of their troops
	 */
	@RequestMapping(value=["/troops/add"], method=[RequestMethod.POST])
	fun addTroopsTo(@RequestParam teamPassword: String, @RequestParam locationId: Int, @RequestParam amount: Int): String {
		val team = getColorOfKey(teamPassword) ?: return "null"
		if (this.world.currentActor != team || !this.isTileIdValid(locationId) || amount <= 0) {
			return "null"
		}
		this.actionLog.add("$team commit $locationId $amount")
		if (this.world.numberOfTroopsToCommit == 0)
			this.actionLog.add("$team end")
		return "${this.world.commitNewTroops(locationId, amount)}"
	}

	/**
	 * Allows the team of the given password to move existing troops to an adjacent or other owned tile
	 * Returns null if the password is wrong or the team isn't allowed to move troops yet or any of the given IDs are wrong or the amount is <= 0
	 * Returns success of moving troops
	 * Troops don't get moved immediately: action of moving troops is queued until the end of the turn
	 */
	@RequestMapping(value=["/troops/move"], method=[RequestMethod.POST])
	fun moveTroops(@RequestParam teamPassword: String, @RequestParam fromId: Int, @RequestParam toId: Int, @RequestParam amount: Int): String {
		val team = getColorOfKey(teamPassword) ?: return "null"
		if (this.world.currentActor != team || !this.isTileIdValid(fromId) || !this.isTileIdValid(toId) || amount <= 0) {
			return "null"
		}
		this.actionLog.add("$team move $fromId $toId $amount")
		return "${this.world.queueTroopsMove(fromId, toId, amount)}"
	}

	/**
	 * Gets the amount of available card cash for the team with the given name
	 * If the given team color doesn't exist, null is returned
	 */
	@RequestMapping(value=["/cards/amount"], method=[RequestMethod.GET])
	fun getAvailableCards(@RequestParam teamColor: String): String {
		val color = this.world.players.firstOrNull { it.name == teamColor } ?: return "null"
		return "${this.world.cardCashValues[color]}"
	}

	/**
	 * Allows a team to spend 1 card cash to connect 2 tiles together
	 * If the team password is wrong or the team is not yet allowed to play a card or any IDs are wrong, null is returned
	 * If the connection already exists or the team doesn't have enough card cash, false is returned
	 * If the connection was successfully made, true is returned
	 */
	@RequestMapping(value=["/cards/connect"], method=[RequestMethod.PUT])
	fun connectTiles(@RequestParam teamPassword: String, @RequestParam tileId1: Int, @RequestParam tileId2: Int): String {
		val team = getColorOfKey(teamPassword) ?: return "null"
		if (this.world.currentActor != team || !this.isTileIdValid(tileId1) || !this.isTileIdValid(tileId2)) {
			return "null"
		}
		this.actionLog.add("$team connect $tileId1 $tileId2")
		return "${this.world.cardConnect(tileId1, tileId2)}"
	}

	/**
	 * Allows a team to spend 2 card cash to inspire an insurgency on a tile
	 * If the team password is wrong or the team is not yet allowed to play or the tile ID is wrong, null is returned
	 * See documentation for World.inspireInsurgency
	 * Success is returned as true; false is only returned if there is not enough money to inspire an insurgency
	 */
	@RequestMapping(value=["/cards/inspireInsurgency"], method=[RequestMethod.POST])
	fun inspireInsurgency(@RequestParam teamPassword: String, @RequestParam tileId: Int): String {
		val team = getColorOfKey(teamPassword) ?: return "null"
		if (this.world.currentActor != team || !this.isTileIdValid(tileId)) {
			return "null"
		}
		this.actionLog.add("$team insurgency $tileId")
		return "${this.world.cardInspireInsurgency(tileId)}"
	}

	/**
	 * Allows a team to spend 4 card cash to remove all connections from a tile
	 * If the team password is wrong or the team is not yet allowed to play a card or the tile ID is wrong, null is returned
	 * If the tile is isolated or the team doesn't have enough card cash, false is returned
	 * True is returned on success
	 */
	@RequestMapping(value=["/cards/disconnect"], method=[RequestMethod.PUT])
	fun disconnectTile(@RequestParam teamPassword: String, @RequestParam tileId: Int): String {
		val team = getColorOfKey(teamPassword) ?: return "null"
		if (this.world.currentActor != team || !this.isTileIdValid(tileId)) {
			return "null"
		}
		this.actionLog.add("$team disconnect $tileId")
		return "${this.world.cardDisconnect(tileId)}"
	}

}
