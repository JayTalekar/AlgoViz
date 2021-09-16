package com.jaytalekar.algoviz.ui.pathfinding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaytalekar.algoviz.domain.pathfinding.AStarRunner
import com.jaytalekar.algoviz.domain.pathfinding.NodeType
import com.jaytalekar.algoviz.domain.pathfinding.PathfindingRunner
import kotlinx.coroutines.*

class PathfindingViewModel : ViewModel() {

    private lateinit var grid: Array<Array<NodeType>>

    private var runner: PathfindingRunner? = null

    private var visitedCells = mutableListOf<Pair<Int, Int>>()
    private var solutionCells = mutableListOf<Pair<Int, Int>>()

    private var currentVisitedIndex: Int = 0

    private var paused: Boolean = false

    private var _algorithmAnimating: MutableLiveData<Boolean> = MutableLiveData()
    val algorithmAnimating: LiveData<Boolean>
        get() = _algorithmAnimating

    private var _destinationReached: MutableLiveData<Boolean> = MutableLiveData(false)
    val destinationReached: LiveData<Boolean>
        get() = _destinationReached

    private var _visitedCell: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    val visitedCell: LiveData<Pair<Int, Int>>
        get() = _visitedCell

    private var _solutionCell: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    val solutionCell: LiveData<Pair<Int, Int>>
        get() = _solutionCell

    private var _blockedCell: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    val blockedCell: LiveData<Pair<Int, Int>>
        get() = _blockedCell

    private var _sourceCell: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    val sourceCell: LiveData<Pair<Int, Int>>
        get() = _sourceCell

    private var _destinationCell: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    val destinationCell: LiveData<Pair<Int, Int>>
        get() = _destinationCell

    private var _cost: MutableLiveData<Int> = MutableLiveData()
    val cost: LiveData<Int>
        get() = _cost

    private var sourcePlacement: Boolean = true
    private var destinationPlacement: Boolean = false
    private var blockPlacement: Boolean = false

    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

    fun setupGrid(rows: Int, columns: Int) {
        grid = Array(rows) {
            Array(columns) {
                NodeType.Empty
            }
        }
    }

    fun onCellStartTouch(coordinate: Pair<Int, Int>) {
        val x = coordinate.first
        val y = coordinate.second
        if (x >= grid.size || y >= grid[0].size) return

        when {
            sourcePlacement -> {
                sourcePlacement = false
                destinationPlacement = true

                grid[x][y] = NodeType.Source
                _sourceCell.value = coordinate
            }

            destinationPlacement -> {
                if (grid[x][y] != NodeType.Source) {
                    destinationPlacement = false
                    blockPlacement = true

                    grid[coordinate.first][coordinate.second] = NodeType.Destination
                    _destinationCell.value = coordinate
                }
            }
            blockPlacement -> {
                if (grid[x][y] == NodeType.Empty) {
                    grid[x][y] = NodeType.Blocked
                    _blockedCell.value = coordinate
                }
            }
        }
    }

    fun onPlayClicked() {
        this.disableBlockPlacement()
        if (runner == null) {
            runAlgorithm()
            paused = false
        } else {
            paused = false
            moveForward()
        }
    }

    fun onPauseClicked() {
        paused = true
    }

    private fun disableBlockPlacement() {
        blockPlacement = false
    }

    private fun runAlgorithm() {
        runner = AStarRunner(grid)

        viewModelScope.launch {
            withContext(defaultDispatcher) {

                runner!!.run(sourceCell.value!!, destinationCell.value!!)

                _destinationReached.postValue(runner!!.destinationReached)

                visitedCells.addAll(runner!!.orderedVisitedNodes)
                solutionCells.addAll(runner!!.solution)

                // Removing source and destination cells from list to avoid animating them
                solutionCells.removeAt(0)
                solutionCells.removeAt(solutionCells.size - 1)

                moveForward()
            }
        }
    }

    private fun moveForward() {
        if (currentVisitedIndex + 1 >= visitedCells.size)
            return

        viewModelScope.launch {
            withContext(defaultDispatcher) {
                _algorithmAnimating.postValue(true)

                for (i in currentVisitedIndex until visitedCells.size) {
                    currentVisitedIndex = i

                    if (!paused) {
                        _visitedCell.postValue(visitedCells[i])

                        delay(250)
                    } else return@withContext

                }

                _algorithmAnimating.postValue(false)
            }
        }
    }

    fun animateSolutionCells() {

        viewModelScope.launch {
            withContext(defaultDispatcher) {

                solutionCells.forEach { coordinate ->
                    _solutionCell.postValue(coordinate)

                    delay(200)
                }
            }

            _cost.value = runner!!.solutionCost
        }
    }
}