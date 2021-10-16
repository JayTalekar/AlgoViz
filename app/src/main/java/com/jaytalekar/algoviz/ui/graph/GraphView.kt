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

    lateinit var adjacencyMatrix: Array<Array<Boolean>>
        private set

    var onAdjacencyMatrixUpdated: (() -> Unit)? = null

    var isAnimating: Boolean = false

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
    private val visitedVertexColor = resources.getColor(R.color.purple)
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

    // Default Vertex Attributes
    private var edgeColor = resources.getColor(R.color.barn_red)
    private val draggingEdgeColor = resources.getColor(R.color.dark_goldenrod)
    private val traversedEdgeColor = resources.getColor(R.color.flame)

    private var edgePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = edgeColor
        strokeWidth = 20f
        elevation = 0f
    }
    // Default Vertex Attributes End


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

    fun animateTraversedEdge(v1: Int, v2: Int) {
        var traversedEdge: EdgeItem? = null

        val startVertex = vertexItemList[v1]
        val endVertex = vertexItemList[v2]

        for (edge in startVertex.edgeList) {
            if (edge.endVertex == endVertex) {
                traversedEdge = edge
                break
            } else if (edge.startVertex == endVertex) {
                traversedEdge = edge
                var temp = traversedEdge.x1
                traversedEdge.x1 = traversedEdge.x2
                traversedEdge.x2 = temp

                temp = traversedEdge.y1
                traversedEdge.y1 = traversedEdge.y2
                traversedEdge.y2 = temp
            }
        }

        traversedEdge?.apply {
            val slope = (y2 - y1) / (x2 - x1)
            val slopeX1 = slope * x1

            val lengthProp = PropertyValuesHolder.ofFloat("length", x1, x2)

            val colorProp = PropertyValuesHolder.ofObject(
                "color",
                ArgbEvaluator(),
                edgeColor,
                traversedEdgeColor
            )

            ObjectAnimator.ofPropertyValuesHolder(colorProp, lengthProp).apply {
                duration = 1500

                addUpdateListener {
                    x2 = it.getAnimatedValue("length") as Float
                    y2 = slope * x2 - slopeX1 + y1

                    color = it.getAnimatedValue("color") as Int

                    invalidate()
                }

                interpolator = AccelerateInterpolator()
            }.start()
        }
    }

    fun animateVisitedVertex(vertexValue: Int) {
        val visitedVertex: VertexItem = vertexItemList[vertexValue]

        val colorProp = PropertyValuesHolder.ofObject(
            "color",
            ArgbEvaluator(),
            vertexColor,
            visitedVertexColor
        )

        ObjectAnimator.ofPropertyValuesHolder(colorProp).apply {
            duration = 500

            addUpdateListener {
                visitedVertex.color = it.getAnimatedValue("color") as Int

                invalidate()
            }

            interpolator = AccelerateInterpolator()
        }.start()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (isAnimating)
            return false

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

        updateAdjacencyMatrix(numVertices)

        val vertex = VertexItem(
            numVertices, coordinate.first, coordinate.second,
            mutableListOf(),
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
        draggingVertex?.apply {
            x = coordinate.first
            y = coordinate.second

            for (edge in edgeList) {
                if (edge.startVertex == this) {
                    val offset = getEdgeOffsets(x, y, edge.x2, edge.y2)

                    edge.x1 = x + offset.first
                    edge.y1 = y + offset.second

                    edge.x2 = edge.endVertex!!.x - offset.first
                    edge.y2 = edge.endVertex.y - offset.second

                } else if (edge.endVertex == this) {
                    val offset = getEdgeOffsets(edge.x1, edge.y1, x, y)

                    edge.x1 = edge.startVertex.x + offset.first
                    edge.y1 = edge.startVertex.y + offset.second

                    edge.x2 = x - offset.first
                    edge.y2 = y - offset.second
                }
            }
        }
    }

    private fun addDraggingEdge(startVertex: VertexItem, currentPosition: Pair<Float, Float>) {
        val x1 = startVertex.x
        val y1 = startVertex.y
        val x2 = currentPosition.first
        val y2 = currentPosition.second

        val offset = getEdgeOffsets(x1, y1, x2, y2)
        val x = offset.first
        val y = offset.second

        draggingEdge = EdgeItem(
            startVertex, null,
            x1 + x, y1 + y,
            x2, y2,
            draggingEdgeColor
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
        for (edge in edgeItemList) {
            if ((edge.startVertex == startVertex && edge.endVertex == endVertex)
                || (edge.startVertex == endVertex && edge.endVertex == startVertex)
            )
                return
        }

        numEdges++

        updateAdjacencyMatrix(startVertex.number, endVertex.number)

        val x1 = startVertex.x
        val y1 = startVertex.y
        val x2 = endVertex.x
        val y2 = endVertex.y

        val offset = getEdgeOffsets(x1, y1, x2, y2)
        val x = offset.first
        val y = offset.second

        val edge = EdgeItem(
            startVertex, endVertex,
            x1 + x, y1 + y,
            x2 - x, y2 - y,
            edgeColor
        )

        edgeItemList.add(edge)

        startVertex.edgeList.add(edge)
        endVertex.edgeList.add(edge)
    }

    private fun updateAdjacencyMatrix(numVertices: Int) {
        if (numVertices == 1) {
            adjacencyMatrix = Array(1) {
                Array(1) { pos -> false }
            }
            return
        }

        val newAdjacencyMatrix = Array(numVertices) { row ->
            Array(numVertices) { col ->
                if (row < adjacencyMatrix.size && col < adjacencyMatrix[0].size)
                    adjacencyMatrix[row][col]
                else false
            }
        }

        adjacencyMatrix = newAdjacencyMatrix

        onAdjacencyMatrixUpdated?.invoke()
    }

    private fun updateAdjacencyMatrix(startVertex: Int, endVertex: Int) {
        if (startVertex <= adjacencyMatrix.size || endVertex <= adjacencyMatrix.size) {
            adjacencyMatrix[startVertex - 1][endVertex - 1] = true
            adjacencyMatrix[endVertex - 1][startVertex - 1] = true
        }

        onAdjacencyMatrixUpdated?.invoke()
    }

    fun resetView() {
        this.numVertices = 0
        this.vertexItemList.clear()
        this.numEdges = 0
        this.edgeItemList.clear()

        invalidate()
    }

    private fun getEdgeOffsets(x1: Float, y1: Float, x2: Float, y2: Float): Pair<Float, Float> {
        val dx = x2 - x1

        val slope = (y2 - y1) / (x2 - x1)
        var x = cos(atan(slope)) * vertexRadius
        var y = sin(atan(slope)) * vertexRadius

        if (dx < 0) {
            x = -x
            y = -y
        }

        return x to y
    }

    private fun fromDpToPx(dp: Int) = context.resources.displayMetrics.density * dp

}