package de.hpled.threadtostl

import com.sun.javafx.geom.Vec2d
import com.sun.javafx.geom.Vec2f

class Vector2 : Vec2f {

    constructor() : super()
    constructor(length: Float) : super(length, 0f)

    fun rotate(degrees: Float) : Vector2 {
        val deg = Math.toRadians(degrees.toDouble())
        val nx = (x * Math.cos(deg) - y * Math.sin(deg))
        val ny = (x * Math.sin(deg) + y * Math.cos(deg))
        x = nx.toFloat()
        y = ny.toFloat()
        return this
    }

}