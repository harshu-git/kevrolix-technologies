package com.antiphonesnatcher.app.data

/**
 * 4-Zone Screen Quadrant definitions for stealth tap sequence recovery.
 *
 * Screen is logically partitioned into 4 quadrants:
 * 1: Top-Left (TL)
 * 2: Top-Right (TR)
 * 3: Bottom-Left (BL)
 * 4: Bottom-Right (BR)
 */
object TapSequenceHelper {

    const val DEFAULT_SEQUENCE = ""

    fun getZoneFromCoordinates(x: Float, y: Float, width: Float, height: Float): Int {
        val midX = width / 2f
        val midY = height / 2f
        return if (y < midY) {
            if (x < midX) 1 else 2
        } else {
            if (x < midX) 3 else 4
        }
    }

    fun getZoneName(zone: Int): String {
        return when (zone) {
            1 -> "Top-Left"
            2 -> "Top-Right"
            3 -> "Bottom-Left"
            4 -> "Bottom-Right"
            else -> "Zone $zone"
        }
    }

    fun getZoneShortName(zone: Int): String {
        return when (zone) {
            1 -> "TL"
            2 -> "TR"
            3 -> "BL"
            4 -> "BR"
            else -> "$zone"
        }
    }

    fun parseSequence(seqString: String?): List<Int> {
        if (seqString.isNullOrBlank()) return emptyList()
        return seqString.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..4 }
    }

    fun serializeSequence(zones: List<Int>): String {
        return zones.joinToString(",")
    }

    fun formatSequence(seqString: String?): String {
        val list = parseSequence(seqString)
        if (list.isEmpty()) return "Not configured"
        return list.joinToString(" → ") { getZoneShortName(it) }
    }
}
