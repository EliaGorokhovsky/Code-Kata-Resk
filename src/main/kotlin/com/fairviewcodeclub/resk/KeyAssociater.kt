package com.fairviewcodeclub.resk

import com.fairviewcodeclub.resk.logic.ReskColor

/**
 * Converts an API key into a ReskColor
 */
fun getColorOfKey(key: String): ReskColor? {
    return when (key) {
		System.getenv()["RED_PASSWORD"] -> ReskColor.RED
		System.getenv()["BLUE_PASSWORD"] -> ReskColor.BLUE
		System.getenv()["GREEN_PASSWORD"] -> ReskColor.GREEN
		System.getenv()["YELLOW_PASSWORD"] -> ReskColor.YELLOW
        else -> null
    }
}
