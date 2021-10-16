package com.jaytalekar.algoviz.ui.graph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaytalekar.algoviz.domain.graph.BFSRunner
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

    fun runAlgorithm() {
        adjacencyMatrix?.let {
            traversalRunner = BFSRunner(it)
            traversalRunner.run()

            animateGraph()
        }
    }

    private fun animateGraph() {

        viewModelScope.launch {
            withContext(defaultDispatcher) {
                _animationInProgress.postValue(true)

                async {
                    for (node in traversalRunner.orderedVisitedNodes) {
                        _visitedVertex.postValue(node)
                        delay(1500)
                    }
                }

                async {
                    for (edge in traversalRunner.orderedTraversedEdges) {
                        _traversedEdge.postValue(edge)
                        delay(1500)
                    }
                }

                _animationInProgress.postValue(false)
            }
        }
    }

}