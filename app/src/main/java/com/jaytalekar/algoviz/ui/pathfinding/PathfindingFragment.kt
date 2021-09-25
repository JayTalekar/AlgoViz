package com.jaytalekar.algoviz.ui.pathfinding

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jaytalekar.algoviz.R
import com.jaytalekar.algoviz.domain.pathfinding.AStarRunner
import com.jaytalekar.algoviz.domain.pathfinding.Algorithms

class PathfindingFragment : Fragment() {

    companion object {
        const val TAG = "PathfindingFragment"
    }

    private lateinit var rootView: View

    private lateinit var controlsBottomSheet: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var bottomSheetCollapsed: Boolean = true

    private lateinit var viewModel: PathfindingViewModel

    private lateinit var gridView: GridView
    private lateinit var tvPrompt: TextView

    private lateinit var ivPlayPause: ImageView
    private lateinit var animationSeekBar: SeekBar
    private lateinit var algorithmSpinner: Spinner
    private lateinit var heuristicSpinner: Spinner
    private lateinit var tvReset: TextView

    private var destinationReached: Boolean = false
    private var paused: Boolean = false
    private var playing: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_pathfinding, container, false)

        setupGrid()
        setupBottomSheet()
        setupControls()

        algorithmSpinner.setSelection(0)
        heuristicSpinner.setSelection(0)

        viewModel = PathfindingViewModel()

        viewModel.sourceCell.observe(viewLifecycleOwner, { coordinate ->
            gridView.animateSourceCell(coordinate)
            showDestinationLabel()
        })

        viewModel.destinationCell.observe(viewLifecycleOwner, { coordinate ->
            gridView.animateDestinationCell(coordinate)
            showHideLabel(false)
            showHideControls(true)
        })

        viewModel.blockedCell.observe(viewLifecycleOwner, { coordinate ->
            gridView.animateBlockedCell(coordinate)
        })

        viewModel.visitedCells.observe(viewLifecycleOwner, { coordinates ->
            gridView.animateVisitedCells(*coordinates.toTypedArray())
        })

        viewModel.removedVisitedCells.observe(viewLifecycleOwner, { coordinates ->
            gridView.animateRemoveVisitedCells(*coordinates.toTypedArray())
        })

        viewModel.solutionCell.observe(viewLifecycleOwner, { coordinate ->
            gridView.animateSolutionCells(coordinate)
        })

        viewModel.destinationReached.observe(viewLifecycleOwner, { destinationReached ->
            this.destinationReached = destinationReached
            if (destinationReached) {
                viewModel.animateSolutionCells()
            } else {
                showHideControls(false)
                showHideLabel(true)
                showDestinationNotReachedLabel()
            }
        })

        viewModel.cost.observe(viewLifecycleOwner, { cost ->
            showHideControls(false)
            showHideLabel(true)
            showCostLabel(cost)
        })

        viewModel.numVisitedCells.observe(viewLifecycleOwner, { numVisitedCells ->
            animationSeekBar.max = numVisitedCells - 1
        })

        viewModel.currentVisitedIndex.observe(viewLifecycleOwner, { currentIndex ->
            animationSeekBar.progress = currentIndex
        })

        return rootView
    }

    private fun setupGrid() {
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

    private fun setupBottomSheet() {
        controlsBottomSheet = rootView.findViewById(R.id.controls_bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(controlsBottomSheet)
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                bottomSheetCollapsed = newState == BottomSheetBehavior.STATE_COLLAPSED
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                bottomSheetCollapsed = false
            }
        })

        controlsBottomSheet.viewTreeObserver.addOnGlobalLayoutListener {
            val view = controlsBottomSheet.getChildAt(1)
            bottomSheetBehavior.setPeekHeight(view.bottom)
        }

    }

    private fun setupControls() {
        ivPlayPause = rootView.findViewById(R.id.btn_play_pause)
        ivPlayPause.setOnClickListener(onClickListener)

        animationSeekBar = rootView.findViewById(R.id.animation_seekbar)
        animationSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)

        algorithmSpinner = rootView.findViewById(R.id.algo_spinner)
        ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.pathfinding_algorithms,
            R.layout.custom_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            algorithmSpinner.adapter = adapter
        }
        algorithmSpinner.onItemSelectedListener = algoItemSelectedListener

        heuristicSpinner = rootView.findViewById(R.id.heuristics_spinner)
        ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.pathfinding_heuristics,
            R.layout.custom_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            heuristicSpinner.adapter = adapter
        }
        heuristicSpinner.onItemSelectedListener = heuristicItemSelectedListener

        tvReset = rootView.findViewById(R.id.tv_reset)
        tvReset.setOnClickListener(onClickListener)
    }

    private val onClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.btn_play_pause -> {
                when {
                    playing -> onPlayClicked()
                    paused -> onPauseClicked()
                }
            }

            R.id.tv_reset -> {

            }
        }
    }

    private val onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                onPauseClicked()
                viewModel.onSeekBarChanged(progress)
                if (progress == seekBar.max)
                    viewModel.animateSolutionCells()
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private val algoItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            viewModel.setupAlgorithm(
                when (position) {
                    0 -> Algorithms.AStar
                    1 -> Algorithms.BestFirstSearch
                    2 -> Algorithms.BFS
                    3 -> Algorithms.DFS
                    else -> Algorithms.AStar
                }
            )

            if (position == 2 || position == 3) {
                heuristicSpinner.visibility = View.GONE
                viewModel.setupRunner()
            } else {
                heuristicSpinner.visibility = View.VISIBLE
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    private val heuristicItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            viewModel.setupHeuristics(
                when (position) {
                    0 -> AStarRunner.Heuristic.Manhattan
                    1 -> AStarRunner.Heuristic.Octile
                    2 -> AStarRunner.Heuristic.Chebyshev
                    3 -> AStarRunner.Heuristic.Euclidean
                    else -> AStarRunner.Heuristic.Manhattan
                }
            )

            viewModel.setupRunner()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
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

    private fun showHideLabel(show: Boolean) {
        if (show) tvPrompt.visibility = View.VISIBLE
        else tvPrompt.visibility = View.INVISIBLE
    }

    private fun showHideControls(show: Boolean) {
        if (show) {
            val translationAnimator =
                ObjectAnimator.ofFloat(controlsBottomSheet, View.TRANSLATION_Y, 300f, 0f)
            translationAnimator.interpolator = AnticipateOvershootInterpolator(1.5f)
            translationAnimator.duration = 1000
            translationAnimator.start()

            controlsBottomSheet.visibility = View.VISIBLE
            controlsBottomSheet.isClickable = true
        } else {
            val translationAnimator =
                ObjectAnimator.ofFloat(controlsBottomSheet, View.TRANSLATION_Y, 0f, 300f)
            translationAnimator.interpolator = AnticipateOvershootInterpolator(1.5f)
            translationAnimator.duration = 1000
            translationAnimator.start()

            translationAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    controlsBottomSheet.visibility = View.INVISIBLE
                    controlsBottomSheet.isClickable = false
                    bottomSheetCollapsed = true
                }

                override fun onAnimationCancel(animation: Animator?) {}

                override fun onAnimationRepeat(animation: Animator?) {}
            })
        }
    }

    private fun onPlayClicked() {
        showPauseIcon()
        paused = true
        playing = false
        viewModel.onPlayClicked()
        showAnimationSeekBar()
        collapseBottomSheet()
    }

    private fun showPauseIcon() = ivPlayPause.setImageResource(android.R.drawable.ic_media_pause)

    private fun showAnimationSeekBar() {
        if (animationSeekBar.visibility == View.INVISIBLE)
            animationSeekBar.visibility = View.VISIBLE
    }

    private fun collapseBottomSheet() {
        bottomSheetBehavior.isDraggable = false
        if (!bottomSheetCollapsed)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        else return
    }

    private fun onPauseClicked() {
        showPlayIcon()
        paused = false
        playing = true
        viewModel.onPauseClicked()
    }

    private fun showPlayIcon() = ivPlayPause.setImageResource(android.R.drawable.ic_media_play)

}