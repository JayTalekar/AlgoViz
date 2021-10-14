package com.jaytalekar.algoviz.ui.graph

data class EdgeItem(
    val startVertex: VertexItem,
    val endVertex: VertexItem?,
    var x1: Float,
    var y1: Float,
    var x2: Float,
    var y2: Float,
    var color: Int
) {

}