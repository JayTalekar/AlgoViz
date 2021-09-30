package com.jaytalekar.algoviz.domain.pathfinding

import java.util.*

class BFSRunner(grid: Array<Array<NodeType>>) : PathfindingRunner(grid) {


    override fun run(source: Pair<Int, Int>, destination: Pair<Int, Int>) {
        val costs = Array(grid.size) {
            Array(grid[0].size) { pos -> Float.MAX_VALUE }
        }

        costs[source.first][source.second] = 0f

        val queue: Queue<GridNode> = LinkedList()

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

                val neighbourCost = costs[node.position.first][node.position.second] + 1f

                if (neighbourCost < costs[neighbour.first][neighbour.second]) {
                    costs[neighbour.first][neighbour.second] = neighbourCost

                    queue.add(
                        GridNode(neighbour, neighbourCost, node)
                    )

                    if (neighbour != destination) {
                        orderedVisitedNodes.add(neighbour)
                        grid[neighbour.first][neighbour.second] = NodeType.Visited
                    }
                }
            }
        }
    }

    override fun findSolutionCost() {
        var solCost = 0f

        for (i in 0 until this.solution.size - 1) {
            solCost += 1
        }

        this.solutionCost = solCost
    }
}