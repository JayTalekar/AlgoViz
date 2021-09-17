package com.jaytalekar.algoviz.ui.pathfinding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jaytalekar.algoviz.R

class PathfindingFragment : Fragment() {

    companion object {
        const val TAG = "PathfindingFragment"
    }

    private lateinit var rootView: View

    private lateinit var viewModel: PathfindingViewModel

    private lateinit var gridView: GridView
    private lateinit var tvPrompt: TextView
    private lateinit var ivPlayPause: ImageView

    private var animationCompleted: Boolean = false
    private var paused: Boolean = false
    private var playing: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_pathfinding, container, false)

        init()

        viewModel = PathfindingViewModel()

        viewModel.sourceCell.observe(viewLifecycleOwner, { coordinate ->
            gridView.animateSourceCell(coordinate)
            showDestinationLabel()
        })

        viewModel.destinationCell.observe(viewLifecycleOwner, { coordinate ->
            gridView.animateDestinationCell(coordinate)
            hideLabel()
            showPlayPauseBtn()
        })

        viewModel.blockedCell.observe(viewLifecycleOwner, { coordinate ->
            gridView.animateBlockedCell(coordinate)
        })

        viewModel.visitedCell.observe(viewLifecycleOwner, { coordinate ->
            gridView.animateVisitedCells(coordinate)
        })

        viewModel.solutionCell.observe(viewLifecycleOwner, { coordinate ->
            gridView.animateSolutionCells(coordinate)
        })

        viewModel.algorithmAnimating.observe(viewLifecycleOwner, {
            animationCompleted = !it
            if (it) showPauseIcon()
            else showPlayIcon()
        })

        viewModel.destinationReached.observe(viewLifecycleOwner, {
            if (it && animationCompleted) {
                viewModel.animateSolutionCells()
            } else {
                hidePlayPauseBtn()
                showLabel()
                showDestinationNotReachedLabel()
            }
        })

        viewModel.cost.observe(viewLifecycleOwner, { cost ->
            hidePlayPauseBtn()
            showLabel()
            showCostLabel(cost)
        })

        return rootView
    }


    private fun init() {
        gridView = rootView.findViewById(R.id.grid_view)
        tvPrompt = rootView.findViewById(R.id.tv_prompt)
        ivPlayPause = rootView.findViewById(R.id.btn_play_pause)
        ivPlayPause.setOnClickListener {
            when {
                playing -> onPlayClicked()
                paused -> onPauseClicked()
            }
        }

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

    private fun showSourceLabel() {
        tvPrompt.text = resources.getString(R.string.select_starting_point)
    }

    private fun showDestinationLabel() {
        tvPrompt.text = resources.getString(R.string.select_destination_point)
    }

    private fun showCostLabel(cost: Float) {
        tvPrompt.text = resources.getString(R.string.solution_cost) + " " + "%.2f".format(cost)
    }

    private fun showDestinationNotReachedLabel() {
        tvPrompt.text = resources.getString(R.string.destination_unreachable)
    }

    private fun hideLabel() {
        tvPrompt.visibility = View.INVISIBLE
    }

    private fun showLabel() {
        tvPrompt.visibility = View.VISIBLE
    }

    private fun showPlayPauseBtn() {
        ivPlayPause.visibility = View.VISIBLE
    }

    private fun hidePlayPauseBtn() {
        ivPlayPause.visibility = View.GONE
    }

    private fun onPlayClicked() {
        if (!animationCompleted) {
            showPauseIcon()
            paused = true
            playing = false
            viewModel.onPlayClicked()
        }
    }

    private fun showPauseIcon() = ivPlayPause.setImageResource(android.R.drawable.ic_media_pause)

    private fun onPauseClicked() {
        if (!animationCompleted) {
            showPlayIcon()
            paused = false
            playing = true
            viewModel.onPauseClicked()
        }
    }

    private fun showPlayIcon() = ivPlayPause.setImageResource(android.R.drawable.ic_media_play)

}