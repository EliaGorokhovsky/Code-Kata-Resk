package com.fairviewcodeclub.resk

import com.fairviewcodeclub.resk.logic.ReskColor
import com.fairviewcodeclub.resk.logic.World
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * The class that handles managing requests for the competition game
 */
@RestController
@RequestMapping(value=["/api"])
class CompetitionController {

	//The world that each player in the competition play in
	val world = World(25, ReskColor.values().toList().shuffled().toTypedArray())

	/**
	 * Gets the order of the teams
	 */
	@RequestMapping(value=["/teams/order"], method=[RequestMethod.GET])
	fun getPlayerOrder(): String {
		return "[${this.world.colors.joinToString(",") { it.name }}]"
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
	@RequestMapping(value=["/teams/numberOfTerritories"], method=[RequestMethod.GET])
	fun getTerritoriesFor(@RequestParam teamColor: String): String {
		val color = this.world.colors.firstOrNull { it.name == teamColor } ?: return "null"
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
	 */
	@RequestMapping(value=["/board/adjacencies"], method=[RequestMethod.GET])
	fun getAdjacenciesForTile(@RequestParam id: Int): String {
		return "[${this.world.getAdjacencies(id).joinToString(",")}]"
	}

	/**
	 * Gets the troops on the tile of the given ID
	 * null means there are no troops
	 */
	@RequestMapping(value=["/board/troops"], method=[RequestMethod.GET])
	fun getTroopsOnTile(@RequestParam id: Int): String {
		return "${this.world.nodes[id].troops}"
	}

	/**
	 * Gets the amount of available card cash for the team with the given name
	 * If the given team color doesn't exist, null is returned
	 */
	@RequestMapping(value=["/cards/amount"], method=[RequestMethod.GET])
	fun getAvailableCards(@RequestParam teamColor: String): String {
		val color = this.world.colors.firstOrNull { it.name == teamColor } ?: return "null"
		return "${this.world.cardCashValues[color]}"
	}

	/**
	 * Allows a team to spend 1 card cash to connect 2 tiles together
	 * If the team password is wrong or the team is not yet allowed to play a card, null is returned
	 * If the connection already exists or the team doesn't have enough card cash, false is returned
	 * If the connection was successfully made, true is returned
	 */
	@RequestMapping(value=["/cards/connect"], method=[RequestMethod.PUT])
	fun connectTiles(@RequestParam teamPassword: String, @RequestParam tileId1: Int, @RequestParam tileId2: Int): String {
		val team = getColorOfKey(teamPassword) ?: return "null"
		if (this.world.currentActor != team) {
			return "null"
		}
		return "${this.world.cardConnect(tileId1, tileId2)}"
	}

}
