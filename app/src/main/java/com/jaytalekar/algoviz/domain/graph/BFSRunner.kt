package com.jaytalekar.algoviz.domain.graph

import java.util.*

class BFSRunner(matrix: Array<Array<Boolean>>) : TraversalRunner(matrix) {

    private val stack: Stack<Int> = Stack()

    override fun run() {
        var currentNode = 0
        stack.push(currentNode)
        orderedVisitedNodes.add(currentNode)

        while (stack.isNotEmpty()) {
            currentNode = stack.pop()

            adjacencyMatrix[currentNode].forEachIndexed { node, connected ->
                if (connected && !orderedVisitedNodes.contains(node)) {
                    stack.push(node)
                    orderedTraversedEdges.add(currentNode to node)
                    orderedVisitedNodes.add(node)
                }
            }
        }
    }

}