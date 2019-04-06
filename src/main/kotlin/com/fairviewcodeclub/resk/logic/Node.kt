package com.fairviewcodeclub.resk.logic

/**
 * A tile on the world
 */
class Node(val id: Int) {

	//The troops that currently on this tile
    var troops: Troops? = null

	/**
	 * Tries to add the new troops to this tile
	 * If the new troops are of the same owner as the current troops, or there no current troops, those troops are just added to the tile
	 * If the troops have different owners, the troops are considered to be attacking this tile
	 */
	fun addTroops(newTroops: Troops) {
		when {
			this.troops == null -> this.troops = newTroops
			this.troops!!.owner == newTroops.owner -> this.troops = Troops(newTroops.owner, this.troops!!.amount + newTroops.amount)
			else -> {
				val defenseForce = this.troops!!
				this.troops = Troops(defenseForce.owner, defenseForce.amount - 3 * newTroops.amount / 4)
				newTroops.amount -= defenseForce.amount / 2
				if (this.troops!!.amount <= 0) {
					if (newTroops.amount > 0) {
						this.troops = newTroops
					} else {
						this.troops = null
					}
				}
			}
		}
	}

}
