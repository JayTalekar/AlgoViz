package com.jaytalekar.algoviz.domain.pathfinding

data class GridNode(
    val position: Pair<Int, Int>,
    val cost: Int,
    val parent: GridNode?
)