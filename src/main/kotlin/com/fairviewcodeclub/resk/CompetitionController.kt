package com.fairviewcodeclub.resk

import com.fairviewcodeclub.resk.logic.ReskColor
import com.fairviewcodeclub.resk.logic.World
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
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
	 * Gets the competition board
	 */
	@RequestMapping(value=["/board"], method=[RequestMethod.GET])
	fun getBoard() : String {
		return this.world.toString()
	}

}
