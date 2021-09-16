package com.jaytalekar.algoviz.ui.pathfinding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jaytalekar.algoviz.domain.pathfinding.NodeType

class PathfindingViewModel : ViewModel() {

    private lateinit var grid: Array<Array<NodeType>>

    private var _algorithmCompleted: MutableLiveData<Boolean> = MutableLiveData(false)
    val algorithmCompleted: LiveData<Boolean>
        get() = _algorithmCompleted

    private var _solutionFound: MutableLiveData<Boolean> = MutableLiveData(false)
    val solutionFound: LiveData<Boolean>
        get() = _solutionFound

    private var _visitedCells: MutableLiveData<List<Pair<Int, Int>>> = MutableLiveData()
    val visitedCells: LiveData<List<Pair<Int, Int>>>
        get() = _visitedCells

    private var _solutionCells: MutableLiveData<List<Pair<Int, Int>>> = MutableLiveData()
    val solutionCells: LiveData<List<Pair<Int, Int>>>
        get() = _solutionCells

    private var _blockedCell: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    val blockedCell: LiveData<Pair<Int, Int>>
        get() = _blockedCell

    private var _sourceCell: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    val sourceCell: LiveData<Pair<Int, Int>>
        get() = _sourceCell

    private var _destinationCell: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    val destinationCell: LiveData<Pair<Int, Int>>
        get() = _destinationCell

    private var sourcePlacement: Boolean = true
    private var destinationPlacement: Boolean = false
    private var blockPlacement: Boolean = false

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
            else -> {
                if (grid[x][y] == NodeType.Empty && blockPlacement) {
                    grid[x][y] = NodeType.Blocked
                    _blockedCell.value = coordinate
                }
            }
        }
    }
}