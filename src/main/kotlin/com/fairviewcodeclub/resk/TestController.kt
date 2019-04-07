package com.fairviewcodeclub.resk

import com.fairviewcodeclub.resk.logic.ReskColor
import com.fairviewcodeclub.resk.logic.World
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * A controller that handles running test environments
 */
@RestController
@RequestMapping(value=["/test/api"])
class TestController {

    //The world that each player in the competition play in
    val redWorld = World(25, arrayOf(ReskColor.RED))
    val greenWorld = World(25, arrayOf(ReskColor.GREEN))
    val blueWorld = World(25, arrayOf(ReskColor.BLUE))
    val yellowWorld = World(25, arrayOf(ReskColor.YELLOW))
    //The log of actions that were taken
    val redActionLog = mutableListOf<String>()
    val greenActionLog = mutableListOf<String>()
    val blueActionLog = mutableListOf<String>()
    val yellowActionLog = mutableListOf<String>()
    //Hashes colors to worlds
    val worldHash = mutableMapOf(
            ReskColor.RED to this.redWorld,
            ReskColor.GREEN to this.greenWorld,
            ReskColor.BLUE to this.blueWorld,
            ReskColor.YELLOW to this.yellowWorld
    )
    //Hashes colors to action logs
    val actionLogHash = mapOf(
            ReskColor.RED to this.redActionLog,
            ReskColor.GREEN to this.greenActionLog,
            ReskColor.BLUE to this.blueActionLog,
            ReskColor.YELLOW to this.yellowActionLog
    )

    /**
     * Returns whether the given tile ID is allowed
     */
    private fun isTileIdValid(tileId: Int, teamPassword: String): Boolean {
        return 0 <= tileId && tileId < this.worldHash[getColorOfKey(teamPassword)]!!.size * this.worldHash[getColorOfKey(teamPassword)]!!.size
    }

    /**
     * Resets a team's test environment
     */
    @RequestMapping(value=["/"], method=[RequestMethod.POST])
    fun reset(@RequestParam teamPassword: String): String {
        this.worldHash[getColorOfKey(teamPassword)!!] = World(25, arrayOf(getColorOfKey(teamPassword)!!))
        return "true"
    }

    /**
     * Gets the order of the teams
     */
    @RequestMapping(value=["/teams/order"], method=[RequestMethod.GET])
    fun getPlayerOrder(@RequestParam teamPassword: String): String {
        return "[${this.worldHash[getColorOfKey(teamPassword)]!!.players.joinToString(",") { "\"${it.name}\"" }}]"
    }

    /**
     * Gets whose turn it is right now
     */
    @RequestMapping(value=["/teams/current"], method=[RequestMethod.GET])
    fun getCurrentActor(@RequestParam teamPassword: String): String {
        return this.worldHash[getColorOfKey(teamPassword)]!!.currentActor.name
    }

    /**
     * Gets the tile IDs for tiles owned by the given team color
     * If the given team color doesn't exist, null is returned
     */
    @RequestMapping(value=["/teams/territories"], method=[RequestMethod.GET])
    fun getTerritoriesFor(@RequestParam teamPassword: String, @RequestParam teamColor: String): String {
        val color = this.worldHash[getColorOfKey(teamPassword)]!!.players.firstOrNull { it.name == teamColor } ?: return "null"
        return "[${this.worldHash[getColorOfKey(teamPassword)]!!.territoriesOwnedBy(color).joinToString(",")}]"
    }

    /**
     * Gets the total number of tiles in the world
     */
    @RequestMapping(value=["/board/size"], method=[RequestMethod.GET])
    fun getBoardSize(@RequestParam teamPassword: String): Int {
        return this.worldHash[getColorOfKey(teamPassword)]!!.nodes.size
    }

    /**
     * Gets the IDs of the tiles that are adjacent to the tile of the given ID
     * If the given ID is wrong, null is returned
     */
    @RequestMapping(value=["/board/adjacencies"], method=[RequestMethod.GET])
    fun getAdjacenciesForTile(@RequestParam teamPassword: String, @RequestParam id: Int): String {
        if (!this.isTileIdValid(id, teamPassword)) {
            return "null"
        }
        return "[${this.worldHash[getColorOfKey(teamPassword)]!!.getAdjacencies(id).joinToString(",")}]"
    }

    /**
     * Gets the troops on the tile of the given ID
     * null means there are no troops or the tile ID is wrong
     */
    @RequestMapping(value=["/board/troops"], method=[RequestMethod.GET])
    fun getTroopsOnTile(@RequestParam teamPassword: String, @RequestParam id: Int): String {
        if (!this.isTileIdValid(id, teamPassword)) {
            return "null"
        }
        return "${this.worldHash[getColorOfKey(teamPassword)]!!.nodes[id].troops}"
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
    fun getActionLog(@RequestParam teamPassword: String): String {
        return "[${this.actionLogHash[getColorOfKey(teamPassword)]!!.joinToString(",") { "\"$it\"" }}]"
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
        if (this.worldHash[getColorOfKey(teamPassword)]!!.currentActor != team || !this.isTileIdValid(locationId, teamPassword) || amount <= 0) {
            return "null"
        }
        this.actionLogHash[getColorOfKey(teamPassword)]!!.add("$team commit $locationId $amount")
        if (this.worldHash[getColorOfKey(teamPassword)]!!.numberOfTroopsToCommit == 0)
            this.actionLogHash[getColorOfKey(teamPassword)]!!.add("$team end")
        return "${this.worldHash[getColorOfKey(teamPassword)]!!.commitNewTroops(locationId, amount)}"
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
        if (this.worldHash[getColorOfKey(teamPassword)]!!.currentActor != team || !this.isTileIdValid(fromId, teamPassword) || !this.isTileIdValid(toId, teamPassword) || amount <= 0) {
            return "null"
        }
        this.actionLogHash[getColorOfKey(teamPassword)]!!.add("$team move $fromId $toId $amount")
        return "${this.worldHash[getColorOfKey(teamPassword)]!!.queueTroopsMove(fromId, toId, amount)}"
    }

	/**
	 * Gets the amount of troops the current player still needs to commit
	 */
	@RequestMapping(value=["/troops/amount"], method=[RequestMethod.GET])
	fun getNumberOfTroopsToCommit(@RequestParam teamPassword: String): String {
		val team = getColorOfKey(teamPassword) ?: return "null"
		return "${this.worldHash[team]!!.numberOfTroopsToCommit}"
	}

    /**
     * Gets the amount of available card cash for the team with the given name
     * If the given team color doesn't exist, null is returned
     */
    @RequestMapping(value=["/cards/amount"], method=[RequestMethod.GET])
    fun getAvailableCards(@RequestParam teamPassword: String, @RequestParam teamColor: String): String {
        val color = this.worldHash[getColorOfKey(teamPassword)]!!.players.firstOrNull { it.name == teamColor } ?: return "null"
        return "${this.worldHash[getColorOfKey(teamPassword)]!!.cardCashValues[color]}"
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
        if (this.worldHash[getColorOfKey(teamPassword)]!!.currentActor != team || !this.isTileIdValid(tileId1, teamPassword) || !this.isTileIdValid(tileId2, teamPassword)) {
            return "null"
        }
        this.actionLogHash[getColorOfKey(teamPassword)]!!.add("$team connect $tileId1 $tileId2")
        return "${this.worldHash[getColorOfKey(teamPassword)]!!.cardConnect(tileId1, tileId2)}"
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
        if (this.worldHash[getColorOfKey(teamPassword)]!!.currentActor != team || !this.isTileIdValid(tileId, teamPassword)) {
            return "null"
        }
        this.actionLogHash[getColorOfKey(teamPassword)]!!.add("$team insurgency $tileId")
        return "${this.worldHash[getColorOfKey(teamPassword)]!!.cardInspireInsurgency(tileId)}"
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
        if (this.worldHash[getColorOfKey(teamPassword)]!!.currentActor != team || !this.isTileIdValid(tileId, teamPassword)) {
            return "null"
        }
        this.actionLogHash[getColorOfKey(teamPassword)]!!.add("$team disconnect $tileId")
        return "${this.worldHash[getColorOfKey(teamPassword)]!!.cardDisconnect(tileId)}"
    }

}
