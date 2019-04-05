package com.fairviewcodeclub.resk.logic

/**
 * A data class that represents some number of troops owned by some player
 */
data class Troops(val owner: ReskColor?, val amount: Int) {

	override fun toString(): String {
		return "{owner:${this.owner?.name},amount:${this.amount}}"
	}

}
