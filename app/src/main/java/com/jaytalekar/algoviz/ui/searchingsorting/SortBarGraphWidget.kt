package com.jaytalekar.algoviz.ui.searchingsorting

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SortBarGraphWidget : BarGraphWidget {

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    )

    suspend fun highlightBars(leftIndex: Int, rightIndex: Int) {
        withContext(Dispatchers.Main) {

            (viewList[leftIndex] as TextView).apply {
                elevation = fromDpToPx(HIGHLIGHT_ELEVATION)
                typeface = Typeface.DEFAULT_BOLD
                paintFlags = Paint.UNDERLINE_TEXT_FLAG
                background = highlightBackground
                setTextColor(highlightTextColor)
            }


            (viewList[rightIndex] as TextView).apply {
                elevation = fromDpToPx(HIGHLIGHT_ELEVATION)
                typeface = Typeface.DEFAULT_BOLD
                paintFlags = Paint.UNDERLINE_TEXT_FLAG
                background = highlightBackground
                setTextColor(highlightTextColor)
            }
        }
    }

    suspend fun animateSwap(adapter: BarGraphAdapter, leftIndex: Int, rightIndex: Int) {
        withContext(Dispatchers.Main) {
            val leftVal = adapter.getItem(leftIndex)
            val rightVal = adapter.getItem(rightIndex)

            val leftBarH = viewList[leftIndex].height
            val rightBarH = viewList[rightIndex].height

            adapter.setItem(leftIndex, rightVal)
            adapter.setItem(rightIndex, leftVal)

            (viewList[leftIndex] as TextView).apply {
                text = rightVal.toString()
            }
            (viewList[rightIndex] as TextView).apply {
                text = leftVal.toString()
            }


            endSet.constrainHeight(viewList[leftIndex].id, rightBarH)
            endSet.constrainHeight(viewList[rightIndex].id, leftBarH)

            setTransition(startSetId, endSetId)
            transition.duration = 500
            this@SortBarGraphWidget.transitionToEnd()
        }
    }

    suspend fun unhighlightBars(leftIndex: Int, rightIndex: Int) {
        withContext(Dispatchers.Main) {
            (viewList[leftIndex] as TextView).apply {
                elevation = fromDpToPx(0)
                typeface = Typeface.DEFAULT
                paintFlags = INVISIBLE
                background = unhighlightBackground
                setTextColor(unhighlightTextColor)
            }


            (viewList[rightIndex] as TextView).apply {
                elevation = fromDpToPx(0)
                typeface = Typeface.DEFAULT
                paintFlags = INVISIBLE
                background = unhighlightBackground
                setTextColor(unhighlightTextColor)
            }
        }
    }
}