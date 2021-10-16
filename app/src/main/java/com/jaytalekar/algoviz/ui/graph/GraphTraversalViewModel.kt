package com.jaytalekar.algoviz.ui.graph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaytalekar.algoviz.domain.graph.BFSRunner
import com.jaytalekar.algoviz.domain.graph.DFSRunner
import com.jaytalekar.algoviz.domain.graph.TraversalRunner
import kotlinx.coroutines.*

class GraphTraversalViewModel : ViewModel() {

    private val defaultDispatcher = Dispatchers.Default

    private var adjacencyMatrix: Array<Array<Boolean>>? = null

    private lateinit var traversalRunner: TraversalRunner

    private var _visitedVertex: MutableLiveData<Int> = MutableLiveData()
    val visitedVertex: LiveData<Int>
        get() = _visitedVertex

    private var _traversedEdge: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    val traversedEdge: LiveData<Pair<Int, Int>>
        get() = _traversedEdge

    private var _animationInProgress: MutableLiveData<Boolean> = MutableLiveData()
    val animationInProgress: LiveData<Boolean>
        get() = _animationInProgress

    fun updateAdjacencyMatrix(matrix: Array<Array<Boolean>>) {
        this.adjacencyMatrix = matrix
    }

    fun runAlgorithm(isBFS: Boolean) {
        if (adjacencyMatrix != null) {
            traversalRunner = if (isBFS) BFSRunner(adjacencyMatrix!!)
            else DFSRunner(adjacencyMatrix!!)

            traversalRunner.run()

            animateGraph()
        }
    }

    private fun animateGraph() {

        viewModelScope.launch {
            withContext(defaultDispatcher) {

                async {
                    _animationInProgress.postValue(true)
                    for (node in traversalRunner.orderedVisitedNodes) {
                        _visitedVertex.postValue(node)
                        delay(1500)
                    }
                    _animationInProgress.postValue(false)
                }

                async {
                    for (edge in traversalRunner.orderedTraversedEdges) {
                        _traversedEdge.postValue(edge)
                        delay(1500)
                    }
                }

            }
        }
    }

    fun reset() {
        adjacencyMatrix = null
    }

}