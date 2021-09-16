package com.jaytalekar.algoviz.ui.pathfinding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.jaytalekar.algoviz.R

class PathfindingFragment : Fragment() {

    companion object {
        const val TAG = "PathfindingFragment"
    }

    private lateinit var rootView: View

    private lateinit var viewModel: PathfindingViewModel

    private lateinit var gridView: GridView
    private lateinit var tvPrompt: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_pathfinding, container, false)

        init()

        viewModel = PathfindingViewModel()

        viewModel.sourceCell.observe(viewLifecycleOwner, Observer { coordinate ->
            gridView.animateSourceCell(coordinate)
            showDestinationLabel()
        })

        viewModel.destinationCell.observe(viewLifecycleOwner, Observer { coordinate ->
            gridView.animateDestinationCell(coordinate)
            hideLabel()
        })

        viewModel.blockedCell.observe(viewLifecycleOwner, Observer { coordinate ->
            gridView.animateBlockedCell(coordinate)
        })

        return rootView
    }


    private fun init() {
        gridView = rootView.findViewById(R.id.grid_view)
        tvPrompt = rootView.findViewById(R.id.tv_prompt)

        gridView.onGridCellStartTouch = { coordinate ->
            viewModel.onCellStartTouch(coordinate)
        }

        gridView.onGridCellTouchMove = { coordinate ->
            viewModel.onCellStartTouch(coordinate)
        }

        gridView.onRowColumnChanged = { rows, columns ->
            viewModel.setupGrid(rows, columns)
        }
    }

    private fun showDestinationLabel() {
        tvPrompt.text = resources.getString(R.string.select_destination_point)
    }

    private fun hideLabel() {
        tvPrompt.visibility = View.INVISIBLE
    }
}