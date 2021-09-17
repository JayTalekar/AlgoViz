package com.jaytalekar.algoviz.domain.pathfinding

import java.util.*
import kotlin.collections.ArrayList

abstract class PathfindingRunner(grid: Array<Array<NodeType>>) {

    var destinationReached: Boolean = false

    lateinit var grid: Array<Array<NodeType>>

    lateinit var visitedNodes: Array<Array<Boolean>>

    var diagonalEnabled: Boolean = false

    var solution: List<Pair<Int, Int>> = ArrayList()

    var solutionCost: Float = -1f

    var orderedVisitedNodes: MutableList<Pair<Int, Int>> = ArrayList()

    init {
        this.setup(grid)
    }

    protected open fun setup(grid: Array<Array<NodeType>>) {
        this.grid = grid
        destinationReached = false
        visitedNodes = Array(grid.size) {
            Array(grid[0].size) {
                false
            }
        }
    }

    abstract fun run(source: Pair<Int, Int>, destination: Pair<Int, Int>)

    fun findSolutionFor(node: GridNode) {
        val sol = mutableListOf<Pair<Int, Int>>()
        var temp = node

        val reversedSol = Stack<Pair<Int, Int>>()

        while (temp.parent != null) {
            reversedSol.push(temp.position)
            temp = temp.parent!!
        }

        reversedSol.push(temp.position)

        while (reversedSol.isNotEmpty())
            sol.add(reversedSol.pop())

        this.solution = sol
    }

    abstract fun findSolutionCost()

    fun clearVisited() {
        for (i in visitedNodes.indices)
            for (j in visitedNodes[i].indices)
                visitedNodes[i][j] = false
    }

    fun findNeighbours(pos: Pair<Int, Int>): List<Pair<Int, Int>> {
        val neighbours = mutableListOf<Pair<Int, Int>>()
        if (diagonalEnabled) {
            val horizontalDir = listOf<Int>(0, 0, -1, 1, -1, 1, 1, -1)
            val verticalDir = listOf<Int>(-1, 1, 0, 0, -1, 1, -1, 1)

            for (h in horizontalDir.indices) {
                val x = pos.first + horizontalDir[h]
                val y = pos.second + verticalDir[h]

                if (x >= grid.size || x < 0 || y >= grid[x].size || y < 0) continue

                neighbours.add(Pair(x, y))
            }

        } else {
            val horizontalDir = listOf<Int>(0, 0, -1, 1)
            val verticalDir = listOf<Int>(-1, 1, 0, 0)

            for (h in horizontalDir.indices) {
                val x = pos.first + horizontalDir[h]
                val y = pos.second + verticalDir[h]

                if (x >= grid.size || x < 0 || y >= grid[x].size || y < 0) continue

                neighbours.add(Pair(x, y))
            }
        }

        return neighbours
    }
}