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
	val world = World(25, ReskColor.values())

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
		val color = ReskColor.values().firstOrNull { it.name == teamColor } ?: return "null"
		return "${this.world.cardCashValues[color]}"
	}

}
