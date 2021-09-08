package com.jaytalekar.algoviz.ui.searchingsorting

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.constraintlayout.motion.widget.TransitionBuilder
import androidx.constraintlayout.widget.ConstraintSet
import com.jaytalekar.algoviz.R
import com.jaytalekar.algoviz.ui.boundBottomToParentBottom

open class BarGraphWidget : MotionLayout {

    companion object {
        const val BAR_TOP_MARGIN = 10
        const val HIGHLIGHT_ELEVATION = 48
    }

    val highlightTextColor = resources.getColor(R.color.white)
    val highlightBackground = resources.getDrawable(R.drawable.bg_dark_border)

    val unhighlightTextColor = resources.getColor(R.color.black)
    val unhighlightBackground = resources.getDrawable(R.drawable.bg_light_border)

    private val scene = MotionScene(this)

    val startSetId = View.generateViewId()
    val startSet = ConstraintSet()

    val endSetId = View.generateViewId()
    val endSet = ConstraintSet()

    var transitionInProgress: Boolean = false
    val transitionId = View.generateViewId()
    val transition = TransitionBuilder.buildTransition(
        scene,
        transitionId,
        startSetId, startSet,
        endSetId, endSet
    )

    private var containerH: Int = 0
    private var containerW: Int = 0

    var viewList: List<View> = listOf()

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