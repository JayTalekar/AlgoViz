package com.jaytalekar.algoviz.ui.pathfinding

import android.animation.ArgbEvaluator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator

class GridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    companion object {
        const val TAG = "GridView"
        const val DEFAULT_BACKGROUND = Color.TRANSPARENT

        const val DEFAULT_CELL_SIZE = 28
        const val DEFAULT_CELL_MARGIN = 4
        const val DEFAULT_CELL_CORNER_RADIUS = 16

        val DEFAULT_CELL_COLOR = Color.parseColor("#E0E0E0")
        val DEFAULT_TRANSITION_COLOR = Color.parseColor("#FFF04C7F")
        val DEFAULT_SOURCE_COLOR = Color.parseColor("#E9AD20")// goldenrod
        val DEFAULT_DESTINATION_COLOR = Color.parseColor("#B83D87") // fandango
        val DEFAULT_BLOCKED_COLOR = Color.parseColor("#720F08") //barn red
        val DEFAULT_VISITED_COLOR = Color.parseColor("#708D81") // xanadu
        val DEFAULT_SOLUTION_COLOR = Color.parseColor("#724E91") // royal purple

        const val DEFAULT_CELL_ANIMATION_DURATION = 500L
    }

    /**
     * A callback when user starts to touch the grid cell
     */
    var onGridCellStartTouch: ((Pair<Int, Int>) -> Unit)? = null

    /**
     * A callback when user moves the touch through the grid cells
     */
    var onGridCellTouchMove: ((Pair<Int, Int>) -> Unit)? = null

    /**
     * A callback when number of rows and columns changes
     * */
    var onRowColumnChanged: ((rows: Int, columns: Int) -> Unit)? = null

    /**
     * A list to store data for colored cells
     */
    private var colorItems = mutableListOf<GridItem>()

    /**
     * A list to store data for the solution cells
     */
    private var solutionCells = mutableListOf<GridItem>()


    // To store the number of rows and columns
    private var gridRows: Int = 0
    private var gridColumns: Int = 0

    // Store default cell Size and Margin
    private var cellSize: Float = fromDpToPx(DEFAULT_CELL_SIZE)
    private var cellMargin: Float = fromDpToPx(DEFAULT_CELL_MARGIN)

    // Store offsets to center the grid in the canvas
    private var verticalOffset: Float = 0f
    private var horizontalOffset: Float = 0f

    // To draw rectangles in the onDraw method
    private var cellRect = RectF()

    // Paint instance to especially draw empty cells
    private val emptyCellPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = DEFAULT_CELL_COLOR
    }

    // Paint instance to draw colored cells
    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = DEFAULT_CELL_COLOR
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        gridColumns = (w - cellMargin).div(cellSize + cellMargin).toInt()
        gridRows = (h - cellMargin).div(cellSize + cellMargin).toInt()

        verticalOffset = (h - gridRows * (cellSize + cellMargin)) / 2
        horizontalOffset = (w - gridColumns * (cellSize + cellMargin)) / 2

        onRowColumnChanged?.invoke(gridRows, gridColumns)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        setMeasuredDimension(
            (gridColumns * cellSize + (gridColumns + 1) * cellMargin).toInt(),
            (gridRows * cellSize + (gridRows + 1) * cellMargin).toInt()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // set the background color of the grid
        canvas.drawColor(DEFAULT_BACKGROUND)

        drawBackgroundCells(canvas)

        // Draw colored cells (source, destination, blocked and visited)
        drawColoredCells(canvas, colorItems)

        // Draw visited cells
        drawColoredCells(canvas, solutionCells)
    }

    private fun drawBackgroundCells(canvas: Canvas) {
        for (i in 0 until gridRows) {
            for (j in 0 until gridColumns) {

                cellRect.set(
                    horizontalOffset + j * cellSize + (j + 1) * cellMargin,
                    verticalOffset + i * cellSize + (i + 1) * cellMargin,
                    horizontalOffset + (j + 1) * cellSize + (j + 1) * cellMargin,
                    verticalOffset + (i + 1) * cellSize + (i + 1) * cellMargin
                )

                canvas.drawRoundRect(
                    cellRect,
                    DEFAULT_CELL_CORNER_RADIUS.toFloat(),
                    DEFAULT_CELL_CORNER_RADIUS.toFloat(),
                    emptyCellPaint
                )
            }
        }
    }

    private fun drawColoredCells(canvas: Canvas, list: MutableList<GridItem>) {
        list.forEach {
            cellRect.set(
                horizontalOffset + it.j * cellSize + (it.j + 1) * cellMargin,
                verticalOffset + it.i * cellSize + (it.i + 1) * cellMargin,
                horizontalOffset + (it.j + 1) * cellSize + (it.j + 1) * cellMargin,
                verticalOffset + (it.i + 1) * cellSize + (it.i + 1) * cellMargin
            )

            cellPaint.color = it.color
            canvas.drawRoundRect(
                cellRect,
                it.cornerRadius.toFloat(), it.cornerRadius.toFloat(),
                cellPaint
            )
        }
    }

    private fun animateCellColors(
        cellColorItems: MutableList<GridItem>,
        startColor: Int,
        endColor: Int,
        vararg cells: Pair<Int, Int>
    ) {
        val nColorItems = Array(
            cells.size
        ) {
            GridItem(
                cells[it].first,
                cells[it].second,
                endColor,
                DEFAULT_CELL_CORNER_RADIUS
            )
        }

        cellColorItems.addAll(nColorItems)

        val colorProperty = PropertyValuesHolder.ofObject(
            "color",
            ArgbEvaluator(),
            startColor,
            endColor
        )

        ValueAnimator().apply {
            setValues(colorProperty)
            duration = DEFAULT_CELL_ANIMATION_DURATION
            addUpdateListener {
                val colorValue = it.getAnimatedValue("color") as Int
                nColorItems.forEach { gridItem ->
                    gridItem.color = colorValue
                }
                invalidate()
            }
            interpolator = AccelerateInterpolator()
        }.start()

    }

    fun animateSourceCell(cell: Pair<Int, Int>) {
        animateCellColors(colorItems, DEFAULT_CELL_COLOR, DEFAULT_SOURCE_COLOR, cell)
    }

    fun animateDestinationCell(cell: Pair<Int, Int>) {
        animateCellColors(colorItems, DEFAULT_CELL_COLOR, DEFAULT_DESTINATION_COLOR, cell)
    }

    fun animateVisitedCells(vararg cells: Pair<Int, Int>) {
        animateCellColors(colorItems, DEFAULT_CELL_COLOR, DEFAULT_VISITED_COLOR, *cells)
    }

    fun animateBlockedCell(cells: Pair<Int, Int>) {
        animateCellColors(colorItems, DEFAULT_CELL_COLOR, DEFAULT_BLOCKED_COLOR, cells)
    }

    fun animateSolutionCells(vararg cells: Pair<Int, Int>) {
        animateCellColors(solutionCells, DEFAULT_VISITED_COLOR, DEFAULT_SOLUTION_COLOR, *cells)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        event?.let {
            val row = (it.y / (cellSize + cellMargin)).toInt()
            val col = (it.x / (cellSize + cellMargin)).toInt()
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    onGridCellStartTouch?.invoke(Pair(row, col))
                }
                MotionEvent.ACTION_MOVE -> {
                    onGridCellTouchMove?.invoke(Pair(row, col))
                }
                else -> {
                }
            }
        }

        return true
    }

    fun fromDpToPx(dp: Int) = context.resources.displayMetrics.density * dp
}