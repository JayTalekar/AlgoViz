package com.jaytalekar.algoviz.ui.pathfinding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaytalekar.algoviz.domain.pathfinding.*
import kotlinx.coroutines.*

class PathfindingViewModel : ViewModel() {

    private lateinit var grid: Array<Array<NodeType>>

    private var algorithm: Algorithms = Algorithms.AStar
    private var heuristic: InformedSearchRunner.Heuristic = InformedSearchRunner.Heuristic.Manhattan
    private var runner: PathfindingRunner? = null

    private var visitedCellsList = mutableListOf<Pair<Int, Int>>()
    private var solutionCellsList = mutableListOf<Pair<Int, Int>>()


    private var paused: Boolean = false

    private var _destinationReached: MutableLiveData<Boolean> = MutableLiveData()
    val destinationReached: LiveData<Boolean>
        get() = _destinationReached

    private var _numVisitedCells: MutableLiveData<Int> = MutableLiveData()
    val numVisitedCells: LiveData<Int>
        get() = _numVisitedCells

    private var _currentVisitedIndex: MutableLiveData<Int> = MutableLiveData(0)
    val currentVisitedIndex: LiveData<Int>
        get() = _currentVisitedIndex

    private var _visitedCells: MutableLiveData<List<Pair<Int, Int>>> = MutableLiveData()
    val visitedCells: LiveData<List<Pair<Int, Int>>>
        get() = _visitedCells

    private var _removedVisitedCells: MutableLiveData<List<Pair<Int, Int>>> = MutableLiveData()
    val removedVisitedCells: LiveData<List<Pair<Int, Int>>>
        get() = _removedVisitedCells

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

    private var _cost: MutableLiveData<Float> = MutableLiveData()
    val cost: LiveData<Float>
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
        viewModelScope.launch {
            if (visitedCellsList.size == 0)
                runAlgorithm()
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

    fun setupAlgorithm(algorithm: Algorithms) {
        this.algorithm = algorithm
    }

    fun setupHeuristics(heuristic: InformedSearchRunner.Heuristic) {
        this.heuristic = heuristic
    }

    fun setupRunner() {

        runner = when (algorithm) {
            Algorithms.AStar -> AStarRunner(grid)
            Algorithms.GreedyBestFirstSearch -> GreedyBestFirstSearch(grid)
            Algorithms.BFS -> BFSRunner(grid)
            Algorithms.Dijkstra -> DijkstraRunner(grid)
        }

        if (this.runner!!::class == AStarRunner::class ||
            this.runner!!::class == GreedyBestFirstSearch::class
        ) {
            (runner as InformedSearchRunner).apply {
                this.heuristic = this@PathfindingViewModel.heuristic
                diagonalEnabled = heuristic != InformedSearchRunner.Heuristic.Manhattan
            }
        }
    }

    private suspend fun runAlgorithm() {

        withContext(defaultDispatcher) {
            visitedCellsList.clear()
            solutionCellsList.clear()

            runner!!.run(sourceCell.value!!, destinationCell.value!!)

            visitedCellsList.addAll(runner!!.orderedVisitedNodes)
            solutionCellsList.addAll(runner!!.solution)

            if (solutionCellsList.isNotEmpty()) {
                // Removing source and destination cells from list to avoid animating them
                solutionCellsList.removeFirst()
                solutionCellsList.removeLast()
            }

            _numVisitedCells.postValue(visitedCellsList.size)
        }
    }

    private suspend fun moveForward() {
        if (_currentVisitedIndex.value!! + 1 >= visitedCellsList.size)
            return

        withContext(defaultDispatcher) {

            for (i in _currentVisitedIndex.value!! until visitedCellsList.size) {
                _currentVisitedIndex.postValue(i)

                if (!paused) {
                    _visitedCells.postValue((listOf(visitedCellsList[i])))

                    delay(250)
                } else return@withContext

            }

            _destinationReached.postValue(runner!!.destinationReached)
        }

    }

    fun onSeekBarChanged(index: Int) {
        if (index > currentVisitedIndex.value!!)
            moveForwardTo(index)
        else if (index < currentVisitedIndex.value!!)
            moveBackwardTo(index)
    }

    private fun moveForwardTo(index: Int) {
        if (_currentVisitedIndex.value!! > index)
            return

        val forwardList = mutableListOf<Pair<Int, Int>>()
        for (i in currentVisitedIndex.value!!..index)
            forwardList.add(visitedCellsList[i])

        _visitedCells.value = forwardList
        _currentVisitedIndex.value = index

    }

    private fun moveBackwardTo(index: Int) {
        if (currentVisitedIndex.value!! < index)
            return

        val backwardList = mutableListOf<Pair<Int, Int>>()
        for (i in index..currentVisitedIndex.value!!)
            backwardList.add(visitedCellsList[i])

        _removedVisitedCells.value = backwardList
        _currentVisitedIndex.value = index
    }

    fun animateSolutionCells() {

        viewModelScope.launch {
            withContext(defaultDispatcher) {

                solutionCellsList.forEach { coordinate ->
                    _solutionCell.postValue(coordinate)

                    delay(200)
                }
            }

            _cost.value = runner!!.solutionCost
        }
    }

    fun updateDestinationStatus() {
        _destinationReached.value = runner!!.destinationReached
    }
}