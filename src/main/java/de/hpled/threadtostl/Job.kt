package de.hpled.threadtostl

enum class Type {
    BOLT, NUT
}

data class Job (
        var type: Type = Type.BOLT,
        var outerDiameter: Double = 20.0,
        var length: Double = 50.0,
        var angle: Double = 45.0,
        var step: Double = 2.0,
        var resolution: Int = 100,
        var wallThinkness: Double = 5.0
) {
    fun getRadius() = (outerDiameter / 2.0).toFloat()
}

