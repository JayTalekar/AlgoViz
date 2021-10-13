package com.jaytalekar.algoviz.ui.graph

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import com.jaytalekar.algoviz.R

class GraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val DEFAULT_BACKGROUND = Color.TRANSPARENT
    }

    init {
        this.isHapticFeedbackEnabled = true
    }

    private val longPressDelay = 500L

    private val transitionColor = resources.getColor(R.color.royal_purple)

    private var draggingVertex: VertexItem? = null
    private var holdedVertex: VertexItem? = null
    private val draggingVertexRunnable: Runnable = Runnable {
        draggingVertex = holdedVertex
        this.performHapticFeedback(
            HapticFeedbackConstants.VIRTUAL_KEY,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    private var vertexItemList = mutableListOf<VertexItem>()
    private var numVertices: Int = 0

    // Default Vertex Attributes
    private val vertexColor = resources.getColor(R.color.fandango)
    private val vertexRadius = fromDpToPx(20)
    private val vertexLabelSize = fromDpToPx(18)

    private var solidPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var vertexLabelPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = vertexLabelSize
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }
    // Default Vertex Attributes End

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        canvas.drawColor(DEFAULT_BACKGROUND)

        drawVertices(canvas)
    }

    private fun drawVertices(canvas: Canvas) {
        for (vertex in vertexItemList) {
            drawVertex(canvas, vertex)
            drawVertexLabel(canvas, vertex)
        }
    }

    private fun drawVertex(canvas: Canvas, vertex: VertexItem) {
        solidPaint.color = vertex.color
        canvas.drawCircle(
            vertex.x, vertex.y, vertex.radius, solidPaint
        )
    }

    private fun drawVertexLabel(canvas: Canvas, vertex: VertexItem) {
        canvas.drawText(
            vertex.number.toString(),
            vertex.x,
            vertex.y - (vertexLabelPaint.ascent() + vertexLabelPaint.descent()) / 2,
            vertexLabelPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                for (vertex in vertexItemList) {
                    val distance = vertex distanceTo (event.x to event.y)

                    if (distance < vertexRadius) {
                        holdedVertex = vertex
                        handler.postDelayed(draggingVertexRunnable, longPressDelay)
                        return true
                    }

                    if (distance < 2 * vertexRadius)
                        return true
                }

                addVertex(event.x to event.y)
            }

            MotionEvent.ACTION_MOVE -> {
                handler.removeCallbacks(draggingVertexRunnable)
                if (draggingVertex != null) {
                    draggingVertex!!.apply {
                        x = event.x
                        y = event.y
                    }
                }

                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(draggingVertexRunnable)
                draggingVertex = null
                holdedVertex = null

                invalidate()
            }
        }

        return true
    }

    private fun addVertex(coordinate: Pair<Float, Float>) {
        numVertices++

        val vertex = VertexItem(
            numVertices, coordinate.first, coordinate.second,
            vertexRadius, vertexColor
        )

        vertexItemList.add(vertex)

        val radiusProp = PropertyValuesHolder.ofFloat("radius", vertexRadius / 2, vertexRadius)

        val colorProp = PropertyValuesHolder.ofObject(
            "color",
            ArgbEvaluator(),
            transitionColor,
            vertexColor
        )

        ObjectAnimator.ofPropertyValuesHolder(radiusProp, colorProp).apply {
            duration = 500

            addUpdateListener {
                vertex.run {
                    this.radius = it.getAnimatedValue("radius") as Float
                    this.color = it.getAnimatedValue("color") as Int
                }

                invalidate()
            }

            interpolator = AccelerateInterpolator()
        }.start()
    }

    private fun fromDpToPx(dp: Int) = context.resources.displayMetrics.density * dp

}