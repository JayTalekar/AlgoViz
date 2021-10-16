package com.jaytalekar.algoviz.domain.graph

import java.util.*

class BFSRunner(matrix: Array<Array<Boolean>>) : TraversalRunner(matrix) {

    private val queue: Queue<Int> = LinkedList()

    override fun run() {
        var currentNode = 0
        queue.add(currentNode)
        orderedVisitedNodes.add(currentNode)

        while (queue.isNotEmpty()) {
            currentNode = queue.remove()

            adjacencyMatrix[currentNode].forEachIndexed { node, connected ->
                if (connected && !orderedVisitedNodes.contains(node)) {
                    queue.add(node)
                    orderedTraversedEdges.add(currentNode to node)
                    orderedVisitedNodes.add(node)
                }
            }
        }
    }

}