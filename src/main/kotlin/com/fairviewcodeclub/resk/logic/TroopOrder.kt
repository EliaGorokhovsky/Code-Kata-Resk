package com.fairviewcodeclub.resk.logic

/**
 * A class that represents an instruction for some troops
 * Will get stored by the world and then executed at the end of a turn
 */
data class TroopOrder(val fromId: Int, val toId: Int, val amount: Int)
