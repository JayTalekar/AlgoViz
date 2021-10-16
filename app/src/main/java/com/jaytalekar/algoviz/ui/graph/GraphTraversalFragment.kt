package com.jaytalekar.algoviz.ui.graph

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jaytalekar.algoviz.R

class GraphTraversalFragment : Fragment() {

    private lateinit var rootView: View

    private lateinit var viewModel: GraphTraversalViewModel

    private lateinit var graphView: GraphView

    private lateinit var tvBFS: TextView
    private lateinit var tvDFS: TextView
    private lateinit var tvTraverse: TextView
    private lateinit var tvReset: TextView

    private var isBFSSelected: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_graph_traversal, container, false)

        viewModel = GraphTraversalViewModel()

        init()

        viewModel.visitedVertex.observe(viewLifecycleOwner, { vertex ->
            graphView.animateVisitedVertex(vertex)
        })

        viewModel.traversedEdge.observe(viewLifecycleOwner, { edge ->
            graphView.animateTraversedEdge(edge.first, edge.second)
        })

        return rootView
    }

    private fun init() {
        graphView = rootView.findViewById(R.id.graph_view)
        graphView.onAdjacencyMatrixUpdated = {
            viewModel.updateAdjacencyMatrix(graphView.adjacencyMatrix)
        }

        tvBFS = rootView.findViewById(R.id.tv_bfs)
        tvDFS = rootView.findViewById(R.id.tv_dfs)
        tvTraverse = rootView.findViewById(R.id.tv_traverse)
        tvReset = rootView.findViewById(R.id.tv_reset)

        tvBFS.typeface = Typeface.DEFAULT_BOLD

        with(onClickListener) {
            tvBFS.setOnClickListener(this)
            tvDFS.setOnClickListener(this)
            tvTraverse.setOnClickListener(this)
            tvReset.setOnClickListener(this)
        }
    }

    private val onClickListener = View.OnClickListener {
        when (it.id) {
            R.id.tv_bfs -> {
                isBFSSelected = true
                tvBFS.typeface = Typeface.DEFAULT_BOLD
                tvDFS.typeface = Typeface.DEFAULT
            }
            R.id.tv_dfs -> {
                isBFSSelected = false
                tvDFS.typeface = Typeface.DEFAULT_BOLD
                tvBFS.typeface = Typeface.DEFAULT
            }
            R.id.tv_traverse -> {
                viewModel.runAlgorithm()
            }
        }
    }

}