package com.jaytalekar.algoviz.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.jaytalekar.algoviz.R

class GraphFragment : Fragment() {

    private lateinit var rootView: View

    private lateinit var tvGraphTraversal: TextView
    private lateinit var tvShortestPath: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_graph, container, false)

        init()

        return rootView
    }

    private fun init() {
        tvGraphTraversal = rootView.findViewById(R.id.tv_graph_traversal)
        tvGraphTraversal.setOnClickListener {
            Navigation.findNavController(rootView)
                .navigate(R.id.action_graphFragment_to_graphTraversalFragment)
        }

        tvShortestPath = rootView.findViewById(R.id.tv_shortest_path)
        tvShortestPath.setOnClickListener {
            Navigation.findNavController(rootView)
                .navigate(R.id.action_graphFragment_to_shortestPathFragment)
        }
    }
}