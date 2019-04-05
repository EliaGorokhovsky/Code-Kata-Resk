package com.fairviewcodeclub.resk.logic

/**
 * A data class that represents some number of troops owned by some player
 */
data class Troops(val owner: ReskColor?, var amount: Int) {

	override fun toString(): String {
		return "{owner:${this.owner?.name},amount:${this.amount}}"
	}

}

/**
 * Gets a new troops object with the amount of both given troops
 * Both given troops must have the same owner
 */
fun consolidate(troops1: Troops, troops2: Troops): Troops {
	assert(troops1.owner == troops2.owner)
	return Troops(troops1.owner, troops1.amount + troops2.amount)
}
