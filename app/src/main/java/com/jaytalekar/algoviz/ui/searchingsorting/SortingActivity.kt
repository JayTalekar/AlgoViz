package com.jaytalekar.algoviz.ui.searchingsorting

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jaytalekar.algoviz.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class SortingActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SortingActivity"
    }

    enum class SortingAlgo {
        BUBBLE_SORT, SELECTION_SORT
    }

    private lateinit var widget: BarGraphWidget
    private lateinit var adapter: BarGraphAdapter

    private lateinit var sortingAlgo: SortingAlgo

    private lateinit var tvRandom: TextView
    private lateinit var tvBubbleSort: TextView
    private lateinit var tvSelectionSort: TextView
    private lateinit var tvSort: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sorting)

        init()
    }

    fun init() {
        widget = findViewById(R.id.barGraphView)

        tvRandom = findViewById(R.id.tv_random_data)
        tvBubbleSort = findViewById(R.id.tv_bubble_sort)
        tvSelectionSort = findViewById(R.id.tv_selection_sort)
        tvSort = findViewById(R.id.tv_sort)

        adapter = BarGraphAdapter(getRandomData(10), this)

        sortingAlgo = SortingAlgo.BUBBLE_SORT
        updateView()

        widget.setupGraph(adapter)
        widget.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                widget.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val height = widget.measuredHeight
                val width = widget.measuredWidth

                Log.d("SortingActivity", "height: $height, width: $width")
                widget.updateLayoutDimensions(height, width)
                widget.updateData(adapter, getRandomData(10))
            }
        })

        tvRandom.setOnClickListener(onClickListener)
        tvBubbleSort.setOnClickListener(onClickListener)
        tvSelectionSort.setOnClickListener(onClickListener)
        tvSort.setOnClickListener(onClickListener)
    }

    private val onClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.tv_random_data -> {
                Log.d(TAG, "Random Data Tapped!!")
                widget.updateData(adapter, getRandomData(10))
            }

            R.id.tv_bubble_sort -> {
                Log.d(TAG, "Bubble Sort Tapped!!")
                sortingAlgo = SortingAlgo.BUBBLE_SORT
                updateView()
            }

            R.id.tv_selection_sort -> {
                Log.d(TAG, "Selection Sort Tapped!!")
                sortingAlgo = SortingAlgo.SELECTION_SORT
                updateView()
            }
            R.id.tv_sort -> {
                Log.d(TAG, "Sort Tapped!!")
                if (sortingAlgo == SortingAlgo.BUBBLE_SORT)
                    bubbleSort(adapter.getAllItems())
                else
                    selectionSort(adapter.getAllItems())
                disableButtons()
            }
        }
    }

    private fun updateView() {
        if (sortingAlgo == SortingAlgo.BUBBLE_SORT) {
            tvBubbleSort.typeface = Typeface.DEFAULT_BOLD
            tvSelectionSort.typeface = Typeface.DEFAULT
        } else {
            tvSelectionSort.typeface = Typeface.DEFAULT_BOLD
            tvBubbleSort.typeface = Typeface.DEFAULT
        }
    }

    private fun getRandomData(size: Int): MutableList<Int> {
        val valueList = mutableListOf<Int>()
        for (i in 0 until size)
            valueList.add(Random.nextInt(10, 100))

        return valueList
    }

    private fun bubbleSort(valueList: List<Int>) {
        CoroutineScope(Dispatchers.Default).launch {
            for (i in 0 until valueList.size) {
                for (j in 0 until valueList.size - i - 1) {

                    widget.highlightBars(j, j + 1)

                    if (valueList[j] > valueList[j + 1]) {
                        widget.animateSwap(adapter, j, j + 1)
                    }
                    delay(550)

                    widget.unhighlightBars(j, j + 1)
                }
            }

            runOnUiThread {
                enableButtons()
            }
        }
    }

    private fun selectionSort(valueList: List<Int>) {
        CoroutineScope(Dispatchers.Default).launch {
            val size: Int = valueList.size

            for (step in 0 until size - 1) {
                var minIndex = step
                for (i in step + 1 until size) {

                    widget.highlightBars(step, i)

                    // Select the minimum element in each loop.
                    if (valueList[i] < valueList[minIndex]) {
                        minIndex = i
                    }
                    delay(550)

                    widget.unhighlightBars(step, i)
                }

                // put min at the correct position
                widget.highlightBars(step, minIndex)

                widget.animateSwap(adapter, step, minIndex)
                delay(550)

                widget.unhighlightBars(step, minIndex)

            }

            runOnUiThread {
                enableButtons()
            }
        }
    }

    private fun disableButtons() {
        tvSort.isClickable = false
        tvRandom.isClickable = false
        tvBubbleSort.isClickable = false
        tvSelectionSort.isClickable = false
    }

    private fun enableButtons() {
        tvSort.isClickable = true
        tvRandom.isClickable = true
        tvBubbleSort.isClickable = true
        tvSelectionSort.isClickable = true
    }

}