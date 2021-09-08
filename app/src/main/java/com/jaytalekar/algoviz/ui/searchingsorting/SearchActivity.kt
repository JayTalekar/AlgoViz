package com.jaytalekar.algoviz.ui.searchingsorting

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jaytalekar.algoviz.R
import com.jaytalekar.algoviz.ui.getRandomData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    enum class SearchAlgo {
        LINEAR_SEARCH, BINARY_SEARCH
    }

    private lateinit var widget: SearchBarGraphWidget
    private lateinit var adapter: BarGraphAdapter

    private lateinit var searchAlgo: SearchAlgo

    private lateinit var tvRandom: TextView
    private lateinit var tvLinearSearch: TextView
    private lateinit var tvBinarySearch: TextView
    private lateinit var etSearch: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        init()
    }

    private fun init() {
        widget = findViewById(R.id.searchBarGraphView)

        tvRandom = findViewById(R.id.tv_random_data)
        tvLinearSearch = findViewById(R.id.tv_linear_search)
        tvBinarySearch = findViewById(R.id.tv_binary_search)
        etSearch = findViewById(R.id.tv_search)

        adapter = BarGraphAdapter(getRandomData(10), this)

        searchAlgo = SearchAlgo.LINEAR_SEARCH
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
        tvLinearSearch.setOnClickListener(onClickListener)
        tvBinarySearch.setOnClickListener(onClickListener)
        etSearch.setOnEditorActionListener(onEditTextClickListener)
    }

    private val onClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.tv_random_data -> {
                if (!widget.transitionInProgress)
                    widget.updateData(adapter, getRandomData(10))
            }

            R.id.tv_linear_search -> {
                searchAlgo = SearchAlgo.LINEAR_SEARCH
                updateView()
            }

            R.id.tv_binary_search -> {
                searchAlgo = SearchAlgo.BINARY_SEARCH
                updateView()
                val sortedList = adapter.getAllItems().toList().sorted()
                widget.updateData(adapter, sortedList)
            }
        }
    }

    private val onEditTextClickListener = TextView.OnEditorActionListener { view, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            val value = etSearch.text.toString().toInt()

            if (!widget.transitionInProgress) {
                if (searchAlgo == SearchAlgo.LINEAR_SEARCH)
                    linearSearch(value, adapter.getAllItems())
                else {
                    binarySearch(value, adapter.getAllItems())
                }
            }

            view.clearFocus()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)

            disableButtons()

            return@OnEditorActionListener true
        }
        return@OnEditorActionListener false
    }

    private fun updateView() {
        if (searchAlgo == SearchAlgo.LINEAR_SEARCH) {
            tvLinearSearch.typeface = Typeface.DEFAULT_BOLD
            tvBinarySearch.typeface = Typeface.DEFAULT
        } else {
            tvBinarySearch.typeface = Typeface.DEFAULT_BOLD
            tvLinearSearch.typeface = Typeface.DEFAULT
        }
    }

    private fun linearSearch(value: Int, valueList: List<Int>) {
        CoroutineScope(Dispatchers.Default).launch {
            var index: Int = -1

            for (i in 0 until valueList.size) {
                widget.highlightBar(i)

                if (value == valueList[i]) {
                    index = i
                    break
                }

                delay(550)

                widget.unhighlightBar(i)
            }

            if (index != -1) {
                delay(2000)
                widget.unhighlightBar(index)
            }

            runOnUiThread {
                enableButtons()
            }

        }
    }

    private fun binarySearch(value: Int, valueList: List<Int>) {
        CoroutineScope(Dispatchers.Default).launch {
            var found: Boolean = false
            var low: Int = 0
            var high: Int = valueList.size - 1
            var mid: Int = (low + high + 1) / 2

            while (low <= high) {
                widget.highlightBars(low, high)
                delay(1000)

                widget.unhighlightBars(low, high)

                if (valueList[mid] == value) {
                    found = true
                    break
                } else if (valueList[mid] < value)
                    low = mid + 1
                else
                    high = mid - 1

                mid = (low + high + 1) / 2
            }

            if (found) {
                widget.highlightBar(mid)
                delay(1000)
                widget.unhighlightBar(mid)
            }

            runOnUiThread {
                enableButtons()
            }
        }
    }

    private fun disableButtons() {
        etSearch.isClickable = false
        tvRandom.isClickable = false
        tvLinearSearch.isClickable = false
        tvBinarySearch.isClickable = false
    }

    private fun enableButtons() {
        etSearch.isClickable = true
        tvRandom.isClickable = true
        tvLinearSearch.isClickable = true
        tvBinarySearch.isClickable = true
    }
}