package com.jaytalekar.algoviz.domain.pathfinding

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

abstract class InformedSearchRunner(grid: Array<Array<NodeType>>) : PathfindingRunner(grid) {

    enum class Heuristic {
        Manhattan,
        Octile,
        Chebyshev,
        Euclidean
    }

    private val sqrt2: Float = 1.414f

    var heuristic: Heuristic = Heuristic.Manhattan

    protected fun heuristics(node: Pair<Int, Int>, destination: Pair<Int, Int>): Float {
        return when (heuristic) {
            Heuristic.Manhattan -> manhattanHeuristic(node, destination).toFloat()
            Heuristic.Octile -> octileHeuristic(node, destination)
            Heuristic.Chebyshev -> chebyshevHeuristic(node, destination)
            Heuristic.Euclidean -> euclideanHeuristic(node, destination)
        }
    }

    private fun manhattanHeuristic(node: Pair<Int, Int>, destination: Pair<Int, Int>): Int {
        return abs(node.first - destination.first) + abs(node.second - destination.second)
    }

    private fun octileHeuristic(node: Pair<Int, Int>, destination: Pair<Int, Int>): Float {
        val dx = abs(node.first - destination.first)
        val dy = abs(node.second - destination.second)

        // D = 1 and D2 = sqrt(2), so h(n) = (dx + dy) + (D2 - D * 2) * min(dx, dy)
        return (dx + dy).toFloat() + (sqrt2 - 2) * min(dx, dy)
    }

    private fun chebyshevHeuristic(node: Pair<Int, Int>, destination: Pair<Int, Int>): Float {
        val dx = abs(node.first - destination.first)
        val dy = abs(node.first - destination.first)

        // D = 1 and D2 = 1, so h(n) = (dx + dy) + (D2 - D * 2) * min(dx, dy)
        return (dx + dy).toFloat() - min(dx, dy)
    }

    private fun euclideanHeuristic(node: Pair<Int, Int>, destination: Pair<Int, Int>): Float {
        val dx = abs(node.first - destination.first)
        val dy = abs(node.first - destination.first)

        // D = 1, so h(n) = D * sqrt(dx * dx + dy * dy)
        return sqrt((dx * dx + dy * dy).toFloat())
    }

    override fun findSolutionCost() {
        var solCost = 0f

        if (heuristic == Heuristic.Manhattan || heuristic == Heuristic.Chebyshev) {
            for (i in 0 until this.solution.size - 1) {
                solCost += 1
            }
        } else {
            for (i in 0 until this.solution.size - 1) {
                solCost += getTransitionCost(solution[i], solution[i + 1])
            }
        }

        this.solutionCost = solCost
    }

    private fun getTransitionCost(currentNode: Pair<Int, Int>, nextNode: Pair<Int, Int>): Float {
        val dx = abs(currentNode.first - nextNode.first)
        val dy = abs(currentNode.second - nextNode.second)

        if (dx == dy)
            return sqrt2
        else
            return 1f
    }
}