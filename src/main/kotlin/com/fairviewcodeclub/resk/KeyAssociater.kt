package com.fairviewcodeclub.resk

import com.fairviewcodeclub.resk.logic.ReskColor

/**
 * Converts an API key into a ReskColor
 */
fun getColorOfKey(key: String): ReskColor? {
    return when (key) {
        System.getenv()["GREEN_PASSWORD"] -> ReskColor.ALIEN_ARMPIT
        System.getenv()["BLUE_PASSWORD"] -> ReskColor.GLAUCOUS
        System.getenv()["YELLOW_PASSWORD"] -> ReskColor.MIKADO
        System.getenv()["RED_PASSWORD"] -> ReskColor.FALU
        else -> null
    }
}