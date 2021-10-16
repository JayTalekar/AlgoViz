package com.jaytalekar.algoviz.domain.graph

abstract class TraversalRunner(var adjacencyMatrix: Array<Array<Boolean>>) {

    var orderedVisitedNodes: MutableList<Int> = mutableListOf()

    var orderedTraversedEdges: MutableList<Pair<Int, Int>> = mutableListOf()

    abstract fun run()

}