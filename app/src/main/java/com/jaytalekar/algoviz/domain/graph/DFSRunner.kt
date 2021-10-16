package com.jaytalekar.algoviz.domain.graph

import java.util.*

class DFSRunner(matrix: Array<Array<Boolean>>) : TraversalRunner(matrix) {

    private val stack: Stack<Int> = Stack()

    override fun run() {
        var currentNode = 0
        orderedVisitedNodes.add(currentNode)

        do {
            val nextNode = getUnvisitedNodes(currentNode)

            if (nextNode != -1) {
                orderedVisitedNodes.add(nextNode)
                orderedTraversedEdges.add(currentNode to nextNode)
                stack.push(currentNode)
                currentNode = nextNode
            } else {
                currentNode = stack.pop()
            }
        } while (stack.isNotEmpty())
    }

    private fun getUnvisitedNodes(node: Int): Int {
        val unvisitedNodesList = mutableListOf<Int>()

        adjacencyMatrix[node].forEachIndexed { adjNode, connected ->
            if (connected && !orderedVisitedNodes.contains(adjNode))
                unvisitedNodesList.add(adjNode)
        }

        if (unvisitedNodesList.isNotEmpty()) {
            return unvisitedNodesList.random()
        }

        return -1
    }
}