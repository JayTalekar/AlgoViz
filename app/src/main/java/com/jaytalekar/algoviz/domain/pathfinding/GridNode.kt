package com.jaytalekar.algoviz.domain.pathfinding

data class GridNode(
    val position: Pair<Int, Int>,
    var cost: Float,
    val parent: GridNode?
) {
    fun compareTo(other: GridNode): Int {
        return if (this.cost > other.cost)
            1
        else if (this.cost < other.cost)
            -1
        else 0
    }
}