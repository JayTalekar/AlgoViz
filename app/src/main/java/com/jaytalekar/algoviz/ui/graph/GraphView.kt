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
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin

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

    private var draggingEdge: EdgeItem? = null
    private var draggingEdgeStartVertex: VertexItem? = null

    private var edgeItemList = mutableListOf<EdgeItem>()
    private var numEdges: Int = 0

    private var edgeColor = resources.getColor(R.color.barn_red)

    private var edgePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = edgeColor
        strokeWidth = 20f
        elevation = 0f
    }


    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        canvas.drawColor(DEFAULT_BACKGROUND)

        drawVertices(canvas)

        drawEdges(canvas)

        drawDraggingEdge(canvas)
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

    private fun drawEdges(canvas: Canvas) {
        for (edge in edgeItemList)
            drawEdge(canvas, edge)
    }

    private fun drawEdge(canvas: Canvas, edge: EdgeItem) {
        with(edge) {
            edgePaint.color = edge.color
            canvas.drawLine(
                x1, y1,
                x2, y2,
                edgePaint
            )
        }
    }

    private fun drawDraggingEdge(canvas: Canvas) {
        draggingEdge?.let {
            edgePaint.color = draggingEdge!!.color
            canvas.drawLine(
                draggingEdge!!.x1, draggingEdge!!.y1,
                draggingEdge!!.x2, draggingEdge!!.y2,
                edgePaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                for (vertex in vertexItemList) {
                    val distance = vertex distanceTo (event.x to event.y)

                    if (distance < vertexRadius) {
                        holdedVertex = vertex
                        handler.postDelayed(draggingVertexRunnable, longPressDelay)

                        draggingEdgeStartVertex = vertex

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
                    updateDraggingVertex(event.x to event.y)
                } else if (draggingEdgeStartVertex != null) {
                    addDraggingEdge(draggingEdgeStartVertex!!, event.x to event.y)
                }

                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(draggingVertexRunnable)
                draggingVertex = null
                holdedVertex = null

                updateDraggingEdge(event.x to event.y)

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

    private fun updateDraggingVertex(coordinate: Pair<Float, Float>) {
        draggingVertex!!.apply {
            x = coordinate.first
            y = coordinate.second
        }
    }

    private fun addDraggingEdge(startVertex: VertexItem, currentPosition: Pair<Float, Float>) {
        val x1 = startVertex.x
        val y1 = startVertex.y
        val x2 = currentPosition.first
        val y2 = currentPosition.second

        val dx = x2 - x1

        val slope = (y2 - y1) / (x2 - x1)
        var x = cos(atan(slope)) * vertexRadius
        var y = sin(atan(slope)) * vertexRadius

        if (dx < 0) {
            x = -x
            y = -y
        }

        draggingEdge = EdgeItem(
            startVertex, null,
            x1 + x, y1 + y,
            x2, y2,
            edgeColor
        )
    }

    private fun updateDraggingEdge(coordinate: Pair<Float, Float>) {

        if (draggingEdgeStartVertex != null) {
            for (vertex in vertexItemList) {
                val distance = vertex distanceTo (coordinate.first to coordinate.second)

                if (distance < vertex.radius) {
                    addEdge(draggingEdgeStartVertex!!, vertex)
                    break
                }
            }

            draggingEdgeStartVertex = null
            draggingEdge = null
        }
    }

    private fun addEdge(startVertex: VertexItem, endVertex: VertexItem) {
        numEdges++

        val x1 = startVertex.x
        val y1 = startVertex.y
        val x2 = endVertex.x
        val y2 = endVertex.y

        val dx = x2 - x1

        val slope = (y2 - y1) / (x2 - x1)
        var x = cos(atan(slope)) * vertexRadius
        var y = sin(atan(slope)) * vertexRadius

        if (dx < 0) {
            x = -x
            y = -y
        }

        val edge = EdgeItem(
            startVertex, endVertex,
            x1 + x, y1 + y,
            x2 - x, y2 - y,
            edgeColor
        )

        edgeItemList.add(edge)
    }

    private fun fromDpToPx(dp: Int) = context.resources.displayMetrics.density * dp

}