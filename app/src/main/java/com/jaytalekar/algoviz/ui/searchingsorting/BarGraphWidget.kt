package com.jaytalekar.algoviz.ui.searchingsorting

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.constraintlayout.motion.widget.TransitionBuilder
import androidx.constraintlayout.widget.ConstraintSet
import com.jaytalekar.algoviz.R
import com.jaytalekar.algoviz.ui.boundBottomToParentBottom
import kotlinx.coroutines.*

class BarGraphWidget : MotionLayout {

    companion object {
        const val BAR_TOP_MARGIN = 10
        const val HIGHLIGHT_ELEVATION = 48
    }

    private val highlightTextColor = resources.getColor(R.color.white)
    private val highlightBackground = resources.getDrawable(R.drawable.bg_dark_border)

    private val unhighlightTextColor = resources.getColor(R.color.black)
    private val unhighlightBackground = resources.getDrawable(R.drawable.bg_light_border)

    private val scene = MotionScene(this)

    private val startSetId = View.generateViewId()
    private val startSet = ConstraintSet()

    private val endSetId = View.generateViewId()
    private val endSet = ConstraintSet()

    private var transitionInProgress: Boolean = false
    private val transitionId = View.generateViewId()
    private val transition = TransitionBuilder.buildTransition(
        scene,
        transitionId,
        startSetId, startSet,
        endSetId, endSet
    )

    private var containerH: Int = 0
    private var containerW: Int = 0

    private var viewList: List<View> = listOf()

    private val transitionListener = object : TransitionListener {
        override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
            transitionInProgress = true
        }

        override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
        }

        override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
            startSet.clone(endSet)
            transitionInProgress = false
        }

        override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
        }
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setupGraph(adapter: BarGraphAdapter) {
        viewList = getViewList(adapter)
        viewList.forEach { view ->
            view.id = View.generateViewId()
            this.addView(view)
        }

        startSet.clone(this)

        endSet.clone(this)

        val size = fromDpToPx(40).toInt()

        for (i in 0 until adapter.count) {
            startSet.constrainHeightWidth(viewList[i].id, size, size)
            startSet.setVisibility(viewList[i].id, View.INVISIBLE)

            boundBottomToParentBottom(viewList[i], startSet)
            boundBottomToParentBottom(viewList[i], endSet)
        }

        startSet.createHorizontalChain(
            ConstraintSet.PARENT_ID,
            ConstraintSet.LEFT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.RIGHT,
            viewList.map { it.id }.toIntArray(),
            FloatArray(adapter.count) { 1f },
            LayoutParams.CHAIN_SPREAD
        )

        endSet.createHorizontalChain(
            ConstraintSet.PARENT_ID,
            ConstraintSet.LEFT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.RIGHT,
            viewList.map { it.id }.toIntArray(),
            FloatArray(adapter.count) { 1f },
            LayoutParams.CHAIN_SPREAD
        )

        transition.duration = 1000

        scene.addTransition(transition)
        scene.setTransition(transition)

        setTransition(transition)
        setScene(scene)

        this.addTransitionListener(transitionListener)
    }

    fun updateLayoutDimensions(height: Int, width: Int) {
        containerW = width
        containerH = height
    }

    fun updateData(adapter: BarGraphAdapter, valueList: List<Int>) {
        val heightList = calculateHeight(valueList)
        val constantWidth = calculateWidth(adapter.count)

        valueList.forEachIndexed { index, value ->
            adapter.setItem(index, value)
            (viewList[index] as TextView).text = value.toString()
        }

        viewList.forEachIndexed { index, view ->
            endSet.constrainHeightWidth(view.id, heightList[index], constantWidth)
            endSet.setVisibility(view.id, View.VISIBLE)
        }

        setTransition(startSetId, endSetId)
        this.transitionToEnd()
    }

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
            this@BarGraphWidget.transitionToEnd()
        }
    }

    suspend fun unhighlightBars(leftIndex: Int, rightIndex: Int) {
        withContext(Dispatchers.Main) {
            (viewList[leftIndex] as TextView).apply {
                elevation = fromDpToPx(0)
                typeface = Typeface.DEFAULT
                paintFlags = View.INVISIBLE
                background = unhighlightBackground
                setTextColor(unhighlightTextColor)
            }


            (viewList[rightIndex] as TextView).apply {
                elevation = fromDpToPx(0)
                typeface = Typeface.DEFAULT
                paintFlags = View.INVISIBLE
                background = unhighlightBackground
                setTextColor(unhighlightTextColor)
            }
        }
    }

    private fun getViewList(adapter: BarGraphAdapter): List<View> {
        val viewList = mutableListOf<View>()

        for (i in 0 until adapter.count) {
            viewList.add(adapter.getView(i, null, null))
        }

        return viewList
    }

    private fun ConstraintSet.constrainHeightWidth(id: Int, height: Int, width: Int) {
        this.constrainHeight(id, height)
        this.constrainWidth(id, width)
    }

    private fun calculateHeight(valueList: List<Int>): List<Int> {
        val heightList = mutableListOf<Int>()

        val maxVal = valueList.maxOrNull()
        val maxHeight = containerH - fromDpToPx(BAR_TOP_MARGIN)

        for (i in valueList.indices) {
            val height = (maxHeight * (valueList[i].toDouble() / maxVal!!)).toInt()
            heightList.add(height)
        }

        return heightList
    }

    private fun calculateWidth(listSize: Int): Int {
        return (containerW.toDouble() / listSize).toInt()
    }

    fun fromDpToPx(dp: Int) = context.resources.displayMetrics.density * dp

    fun fromPxToDp(px: Int) = (px / context.resources.displayMetrics.density).toInt()


}