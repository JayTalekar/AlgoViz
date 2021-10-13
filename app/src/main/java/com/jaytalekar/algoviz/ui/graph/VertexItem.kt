package com.jaytalekar.algoviz.ui.graph

import kotlin.math.sqrt

data class VertexItem(
    var number: Int,
    var x: Float,
    var y: Float,
    var radius: Float,
    var color: Int
) {

    infix fun distanceTo(pos: Pair<Float, Float>): Double {
        val x2 = (x.toDouble() - pos.first.toDouble()) * (x.toDouble() - pos.first.toDouble())
        val y2 = (y.toDouble() - pos.second.toDouble()) * (y.toDouble() - pos.second.toDouble())

        return sqrt(x2 + y2)
    }


    infix fun distanceTo(v: VertexItem): Double {
        val x2 = (x.toDouble() - v.x.toDouble()) * (x.toDouble() - v.x.toDouble())
        val y2 = (y.toDouble() - v.y.toDouble()) * (y.toDouble() - v.y.toDouble())

        return sqrt(x2 + y2)
    }

}