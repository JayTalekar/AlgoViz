package com.jaytalekar.algoviz.domain.pathfinding

import java.util.*
import kotlin.math.abs

class AStarRunner(grid: Array<Array<NodeType>>) : PathfindingRunner(grid) {

    override fun run(source: Pair<Int, Int>, destination: Pair<Int, Int>) {
        val costs = Array(grid.size) {
            Array(grid[it].size) { pos -> Int.MAX_VALUE }
        }

        costs[source.first][source.second] = 0

        val queue = PriorityQueue<GridNode>(30) { a, b ->
            a.cost - b.cost
        }

        queue.add(GridNode(source, 0, null))

        orderedVisitedNodes = mutableListOf()
        destinationReached = false

        while (queue.isNotEmpty()) {
            val node = queue.poll()

            if (node.position == destination) {
                destinationReached = true
                findSolutionFor(node)
                break
            }

            for (neighbour in findNeighbours(node.position)) {
                if (grid[neighbour.first][neighbour.second] == NodeType.Blocked)
                    continue

                val nodeCost = costs[node.position.first][node.position.second] + 1

                if (nodeCost < costs[neighbour.first][neighbour.second]) {
                    costs[neighbour.first][neighbour.second] = nodeCost

                    queue.add(
                        GridNode(
                            neighbour,
                            nodeCost + heuristic(neighbour, destination),
                            node
                        )
                    )

                    if (neighbour != destination)
                        orderedVisitedNodes.add(neighbour)
                }

            }
        }
    }

    private fun heuristic(node: Pair<Int, Int>, destination: Pair<Int, Int>): Int {
        return abs(node.first - destination.first) + abs(node.second - destination.second)
    }
}