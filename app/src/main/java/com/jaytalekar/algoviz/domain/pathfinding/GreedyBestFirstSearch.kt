package com.jaytalekar.algoviz.domain.pathfinding

import java.util.*

class GreedyBestFirstSearch(grid: Array<Array<NodeType>>) : InformedSearchRunner(grid) {

    override fun run(source: Pair<Int, Int>, destination: Pair<Int, Int>) {
        val costs = Array(grid.size) {
            Array(grid[it].size) { pos -> Float.MAX_VALUE }
        }

        costs[source.first][source.second] = 0f

        val queue = PriorityQueue<GridNode>(30) { a, b ->
            a.compareTo(b)
        }

        queue.add(GridNode(source, 0f, null))

        orderedVisitedNodes = mutableListOf()
        destinationReached = false

        while (queue.isNotEmpty()) {
            val node = queue.poll()

            if (node.position == destination) {
                destinationReached = true
                findSolutionFor(node)
                findSolutionCost()
                break
            }

            for (neighbour in findNeighbours(node.position)) {
                if (grid[neighbour.first][neighbour.second] == NodeType.Blocked ||
                    grid[neighbour.first][neighbour.second] == NodeType.Visited
                )
                    continue

                val nodeCost = costs[node.position.first][node.position.second] + 1f

                if (nodeCost < costs[neighbour.first][neighbour.second]) {
                    costs[neighbour.first][neighbour.second] = nodeCost

                    queue.add(
                        GridNode(
                            neighbour,
                            heuristics(neighbour, destination),
                            node
                        )
                    )

                    if (neighbour != destination) {
                        orderedVisitedNodes.add(neighbour)
                        grid[neighbour.first][neighbour.second] = NodeType.Visited
                    }
                }

            }
        }
    }

}