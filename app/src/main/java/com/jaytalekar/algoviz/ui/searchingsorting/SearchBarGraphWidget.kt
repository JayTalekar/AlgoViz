package com.jaytalekar.algoviz.ui.searchingsorting

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchBarGraphWidget : BarGraphWidget {

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    )

    suspend fun highlightBar(index: Int) {
        withContext(Dispatchers.Main) {

            (viewList[index] as TextView).apply {
                elevation = fromDpToPx(HIGHLIGHT_ELEVATION)
                typeface = Typeface.DEFAULT_BOLD
                paintFlags = Paint.UNDERLINE_TEXT_FLAG
                background = highlightBackground
                setTextColor(highlightTextColor)
            }

        }
    }

    suspend fun unhighlightBar(index: Int) {
        withContext(Dispatchers.Main) {

            (viewList[index] as TextView).apply {
                elevation = fromDpToPx(0)
                typeface = Typeface.DEFAULT
                paintFlags = INVISIBLE
                background = unhighlightBackground
                setTextColor(unhighlightTextColor)
            }

        }
    }

    suspend fun highlightBars(fromIndex: Int, toIndex: Int) {
        withContext(Dispatchers.Main) {
            for (index in fromIndex..toIndex) {
                highlightBar(index)
            }
        }
    }

    suspend fun unhighlightBars(fromIndex: Int, toIndex: Int) {
        withContext(Dispatchers.Main) {
            for (index in fromIndex..toIndex) {
                unhighlightBar(index)
            }
        }
    }
}